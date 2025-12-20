package com.ghn.eventmodule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 浩楠
 * @date 2025/6/16
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 事件总线实现
 */
object SharedFlowEventBus {
    val eventFlows = ConcurrentHashMap<String, MutableSharedFlow<Any>>()

    // 记录粘性事件历史（队列）
    val stickyEventQueue = ConcurrentHashMap<String, ArrayDeque<StickyEventWrapper>>()

    // 每种事件的最大缓存数（可配置）
    val maxCacheSizeMap = ConcurrentHashMap<String, Int>()

    private const val DEFAULT_CACHE_SIZE = 10

    data class StickyEventWrapper(val event: Any, val timestamp: Long)

    fun <T : Any> post(event: T) {
        val key = event.javaClass.name
        val flow = eventFlows.getOrPut(key) {
            MutableSharedFlow(extraBufferCapacity = 64)
        }

        // 加入历史队列
        val queue = stickyEventQueue.getOrPut(key) { ArrayDeque() }
        synchronized(queue) {
            val maxSize = maxCacheSizeMap[key] ?: DEFAULT_CACHE_SIZE
            if (queue.size >= maxSize) queue.removeFirst()
            queue.addLast(StickyEventWrapper(event, System.currentTimeMillis()))
        }

        flow.tryEmit(event)
    }

    inline fun <reified T : Any> observe(sticky: Boolean = false): Flow<T> {
        val key = T::class.java.name
        val flow = eventFlows.getOrPut(key) {
            MutableSharedFlow(extraBufferCapacity = 64)
        }.filterIsInstance<T>()

        return if (sticky) {
            flow {
                val queue = stickyEventQueue[key]
                queue?.forEach {
                    emit(it.event as T)
                }
                emitAll(flow)
            }
        } else {
            flow
        }
    }

    inline fun <reified T : Any> observeOnlySticky(): Flow<T> {
        val key = T::class.java.name
        return flow {
            val queue = stickyEventQueue[key]
            queue?.forEach {
                emit(it.event as T)
            }
        }
    }

    inline fun <reified T : Any> setMaxStickyCacheSize(size: Int) {
        val key = T::class.java.name
        maxCacheSizeMap[key] = size
    }

    inline fun <reified T : Any> getStickyEvents(): List<Pair<T, Long>> {
        val key = T::class.java.name
        return stickyEventQueue[key]
            ?.filter { it.event is T }
            ?.map { it.event as T to it.timestamp }
            ?: emptyList()
    }

    inline fun <reified T : Any> clearStickyEvents() {
        val key = T::class.java.name
        stickyEventQueue.remove(key)
    }

    fun clearAllStickyEvents() {
        stickyEventQueue.clear()
    }
}