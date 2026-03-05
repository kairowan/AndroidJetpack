package com.ghn.eventmodule

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


/**
 * @author 浩楠
 * @date 2025/6/16
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 提供统一的发送和订阅接口
 */
object EventChannel {
    @Volatile
    private var errorHandler: ((Throwable) -> Unit)? = null

    /**
     * 设置统一的事件异常处理回调
     */
    fun setErrorHandler(handler: ((Throwable) -> Unit)?) {
        errorHandler = handler
    }

    @PublishedApi
    internal fun handleError(t: Throwable) {
        errorHandler?.invoke(t)
    }

    /**
     * 发送事件
     * @param event 要发送的事件对象
     */
    fun <T : Any> post(event: T) {
        SharedFlowEventBus.post(event)
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
        return SharedFlowEventBus.observe(sticky, consumeSticky)
    }

    /**
     * 仅订阅粘性事件（历史缓存），不订阅后续实时事件
     * @param consumeSticky 是否消费粘性事件
     * @return 事件流
     */
    inline fun <reified T : Any> observeOnlySticky(consumeSticky: Boolean = true): Flow<T> {
        return SharedFlowEventBus.observeOnlySticky(consumeSticky)
    }

    /**
     * 获取某类事件的粘性事件列表（不消费）
     * @return 事件列表，包含事件对象和发送时间戳
     */
    inline fun <reified T : Any> getStickyEvents(): List<Pair<T, Long>> {
        return SharedFlowEventBus.getStickyEvents()
    }

    /**
     * 设置某类事件的最大粘性缓存数量
     * @param size 最大缓存数量，设为 0 或负数将禁用粘性缓存并清除现有缓存
     */
    inline fun <reified T : Any> setMaxStickyCacheSize(size: Int) {
        SharedFlowEventBus.setMaxStickyCacheSize<T>(size)
    }

    /**
     * 清除某类事件的粘性缓存
     */
    inline fun <reified T : Any> clearStickyEvents() {
        SharedFlowEventBus.clearStickyEvents<T>()
    }

    /**
     * 清除所有粘性事件缓存
     */
    fun clearAllStickyEvents() {
        SharedFlowEventBus.clearAllStickyEvents()
    }

    /**
     * 销毁事件总线，取消所有协程并清理资源
     * 通常在 Application.onTerminate() 或测试 tearDown 中调用
     */
    fun destroy() {
        SharedFlowEventBus.destroy()
    }

    /**
     * Fragment 订阅事件的扩展函数
     * 自动绑定 viewLifecycleOwner 生命周期，在 STARTED 状态时收集事件
     *
     * 安全性改进：如果在 view 创建前调用，会延迟到 view 创建后再订阅
     *
     * @param sticky 是否接收粘性事件
     * @param consumeSticky 是否消费粘性事件
     * @param block 事件处理回调
     */
    inline fun <reified T : Any> Fragment.observeEvent(
        sticky: Boolean = false,
        consumeSticky: Boolean = true,
        crossinline block: suspend (T) -> Unit
    ) {
        if (view != null) {
            // view 已创建，直接订阅
            doObserveEvent(sticky, consumeSticky, block)
        } else {
            // view 未创建，延迟到 onViewCreated 后订阅
            viewLifecycleOwnerLiveData.observe(this, object : Observer<LifecycleOwner?> {
                override fun onChanged(owner: LifecycleOwner?) {
                    if (owner != null) {
                        viewLifecycleOwnerLiveData.removeObserver(this)
                        doObserveEvent(sticky, consumeSticky, block)
                    }
                }
            })
        }
    }

    /**
     * Fragment 订阅事件的内部实现
     */
    @PublishedApi
    internal inline fun <reified T : Any> Fragment.doObserveEvent(
        sticky: Boolean,
        consumeSticky: Boolean,
        crossinline block: suspend (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observe<T>(sticky, consumeSticky).collect { event ->
                    try {
                        block(event)
                    } catch (t: Throwable) {
                        handleError(t)
                    }
                }
            }
        }
    }

    /**
     * ComponentActivity 订阅事件的扩展函数
     * 自动绑定生命周期，在 STARTED 状态时收集事件
     *
     * @param sticky 是否接收粘性事件
     * @param consumeSticky 是否消费粘性事件
     * @param block 事件处理回调
     */
    inline fun <reified T : Any> ComponentActivity.observeEvent(
        sticky: Boolean = false,
        consumeSticky: Boolean = true,
        crossinline block: suspend (T) -> Unit
    ) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observe<T>(sticky, consumeSticky).collect { event ->
                    try {
                        block(event)
                    } catch (t: Throwable) {
                        handleError(t)
                    }
                }
            }
        }
    }
}
