package com.ghn.eventmodule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 浩楠
 * @date 2025/6/16
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 事件总线实现
 */
object SharedFlowEventBus {
    @PublishedApi
    internal val eventFlows = ConcurrentHashMap<String, MutableSharedFlow<Any>>()

    // 记录粘性事件历史（队列）
    @PublishedApi
    internal val stickyEventQueue = ConcurrentHashMap<String, ArrayDeque<StickyEventWrapper>>()

    // 每种事件的最大缓存数（可配置）
    @PublishedApi
    internal val maxCacheSizeMap = ConcurrentHashMap<String, Int>()

    @Volatile
    private var scope: CoroutineScope? = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val scopeLock = Any()

    private const val DEFAULT_CACHE_SIZE = 10

    data class StickyEventWrapper(val event: Any, val timestamp: Long)

    /**
     * 获取或重建 CoroutineScope
     * 使用双重检查锁确保线程安全
     */
    private fun getScope(): CoroutineScope {
        return scope ?: synchronized(scopeLock) {
            scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Default).also { scope = it }
        }
    }

    /**
     * 发送事件
     * @param event 要发送的事件对象
     */
    fun <T : Any> post(event: T) {
        val key = event.javaClass.name
        // 使用 computeIfAbsent 确保原子性
        val flow = eventFlows.computeIfAbsent(key) { newSharedFlow() }

        // 加入历史队列
        val maxSize = maxCacheSizeMap[key] ?: DEFAULT_CACHE_SIZE
        if (maxSize > 0) {
            // 使用 computeIfAbsent 确保原子性
            val queue = stickyEventQueue.computeIfAbsent(key) { ArrayDeque() }
            synchronized(queue) {
                if (queue.size >= maxSize) queue.removeFirst()
                queue.addLast(StickyEventWrapper(event, System.currentTimeMillis()))
            }
        }

        if (!flow.tryEmit(event)) {
            getScope().launch {
                flow.emit(event)
            }
        }
        if (maxSize <= 0) {
            cleanupIfIdle(key)
        }
    }

    /**
     * 订阅事件
     * @param sticky 是否接收粘性事件（历史缓存的事件）
     * @param consumeSticky 是否消费粘性事件（消费后其他订阅者将无法收到）
     * @return 事件流
     */
    inline fun <reified T : Any> observe(
        sticky: Boolean = false,
        consumeSticky: Boolean = true
    ): Flow<T> {
        val key = T::class.java.name
        // 使用 computeIfAbsent 确保原子性
        val typedFlow = eventFlows.computeIfAbsent(key) { newSharedFlow() }.filterIsInstance<T>()

        return if (sticky) {
            flow {
                val snapshot = takeStickySnapshot<T>(key, consume = consumeSticky)
                snapshot.forEach { emit(it) }
                emitAll(typedFlow)
            }
        } else {
            typedFlow
        }
    }

    /**
     * 仅订阅粘性事件（历史缓存），不订阅后续实时事件
     * @param consumeSticky 是否消费粘性事件
     * @return 事件流
     */
    inline fun <reified T : Any> observeOnlySticky(consumeSticky: Boolean = true): Flow<T> {
        val key = T::class.java.name
        return flow {
            val snapshot = takeStickySnapshot<T>(key, consume = consumeSticky)
            snapshot.forEach { emit(it) }
        }
    }

    /**
     * 设置某类事件的最大粘性缓存数量
     * @param size 最大缓存数量，设为 0 或负数将禁用粘性缓存并清除现有缓存
     */
    inline fun <reified T : Any> setMaxStickyCacheSize(size: Int) {
        val key = T::class.java.name
        if (size > 0) {
            maxCacheSizeMap[key] = size
        } else {
            maxCacheSizeMap.remove(key)
            stickyEventQueue.remove(key)
            cleanupIfIdle(key)
        }
    }

    /**
     * 获取某类事件的粘性事件列表（不消费）
     * @return 事件列表，包含事件对象和发送时间戳
     */
    inline fun <reified T : Any> getStickyEvents(): List<Pair<T, Long>> {
        val key = T::class.java.name
        val queue = stickyEventQueue[key] ?: return emptyList()
        return synchronized(queue) {
            queue.filter { it.event is T }
                .map { it.event as T to it.timestamp }
        }
    }

    /**
     * 清除某类事件的粘性缓存
     * 注意：不会清除 maxCacheSizeMap 配置
     */
    inline fun <reified T : Any> clearStickyEvents() {
        val key = T::class.java.name
        stickyEventQueue.remove(key)
        // 不再清除 maxCacheSizeMap，保留用户配置
        cleanupIfIdle(key)
    }

    /**
     * 清除所有粘性事件缓存
     * 注意：不会清除 maxCacheSizeMap 配置
     */
    fun clearAllStickyEvents() {
        stickyEventQueue.clear()
        // 不再清除 maxCacheSizeMap，保留用户配置
        cleanupAllIfIdle()
    }

    /**
     * 销毁事件总线，取消所有协程并清理资源
     * 通常在 Application.onTerminate() 或测试 tearDown 中调用
     * 销毁后仍可正常使用，会自动重建 scope
     */
    fun destroy() {
        synchronized(scopeLock) {
            scope?.cancel()
            scope = null
        }
        eventFlows.clear()
        stickyEventQueue.clear()
        maxCacheSizeMap.clear()
    }

    @PublishedApi
    internal fun newSharedFlow(): MutableSharedFlow<Any> {
        return MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = 64
        )
    }

    @PublishedApi
    internal fun <T : Any> takeStickySnapshot(key: String, consume: Boolean): List<T> {
        val queue = stickyEventQueue[key] ?: return emptyList()
        val snapshot = synchronized(queue) {
            val events = queue.map { it.event }
            if (consume) {
                queue.clear()
            }
            events
        }
        if (consume) {
            stickyEventQueue.remove(key)
            // 不再清除 maxCacheSizeMap，保留用户配置
            cleanupIfIdle(key)
        }
        @Suppress("UNCHECKED_CAST")
        return snapshot as List<T>
    }

    /**
     * 使用 computeIfPresent 实现原子检查+移除
     * 避免竞态条件
     */
    @PublishedApi
    internal fun cleanupIfIdle(key: String) {
        eventFlows.compute(key) { _, flow ->
            if (flow == null) {
                null
            } else {
                val hasSticky = hasStickyEvents(key)
                if (flow.subscriptionCount.value == 0 && !hasSticky) null else flow
            }
        }
    }

    @PublishedApi
    internal fun cleanupAllIfIdle() {
        eventFlows.keys.toList().forEach { key ->
            cleanupIfIdle(key)
        }
    }

    @PublishedApi
    internal fun hasStickyEvents(key: String): Boolean {
        val queue = stickyEventQueue[key] ?: return false
        return synchronized(queue) {
            queue.isNotEmpty()
        }
    }
}
