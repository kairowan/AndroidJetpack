package com.ghn.eventmodule

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
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
 *  描述: Flow collect 的辅助扩展函数
 */

    /**
     * 在 LifecycleOwner 的生命周期内收集 Flow
     * 默认在 STARTED 状态时收集，可通过 minState 参数配置
     * @param owner 生命周期持有者
     * @param minState 最小生命周期状态，只有在此状态及以上时才会收集
     * @param block 收集到数据时的回调
     */
    fun <T> Flow<T>.collectIn(
        owner: LifecycleOwner,
        minState: Lifecycle.State = Lifecycle.State.STARTED,
        block: suspend (T) -> Unit
    ) {
        owner.lifecycleScope.launch {
            owner.lifecycle.repeatOnLifecycle(minState) {
                collect { event ->
                    try {
                        block(event)
                    } catch (t: Throwable) {
                        EventChannel.handleError(t)
                    }
                }
            }
        }
    }

    /**
     * 在指定的 CoroutineScope 中收集 Flow
     * 适用于 ViewModel 等非生命周期感知的场景
     *
     * @param scope 协程作用域
     * @param block 收集到数据时的回调
     */
    fun <T> Flow<T>.collectIn(scope: CoroutineScope, block: suspend (T) -> Unit) {
        scope.launch {
            collect { event ->
                try {
                    block(event)
                } catch (t: Throwable) {
                    EventChannel.handleError(t)
                }
            }
        }
    }
