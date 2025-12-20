package com.ghn.eventmodule

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
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
 *  描述: TODO 提供统一的发送和订阅接口
 */
object EventChannel {

    fun <T : Any> post(event: T) {
        SharedFlowEventBus.post(event)
    }

    inline fun <reified T : Any> observe(sticky: Boolean = false): Flow<T> {
        return SharedFlowEventBus.observe(sticky)
    }

    inline fun <reified T : Any> observeOnlySticky(): Flow<T> {
        return SharedFlowEventBus.observeOnlySticky()
    }

    inline fun <reified T : Any> getStickyEvents(): List<Pair<T, Long>> {
        return SharedFlowEventBus.getStickyEvents()
    }

    inline fun <reified T : Any> setMaxStickyCacheSize(size: Int) {
        SharedFlowEventBus.setMaxStickyCacheSize<T>(size)
    }

    inline fun <reified T : Any> clearStickyEvents() {
        SharedFlowEventBus.clearStickyEvents<T>()
    }

    inline fun <reified T : Any> Fragment.observeEvent(
        sticky: Boolean = false,
        crossinline block: suspend (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observe<T>(sticky).collect {
                    block(it)
                }
            }
        }
    }

    inline fun <reified T : Any> ComponentActivity.observeEvent(
        sticky: Boolean = false,
        crossinline block: suspend (T) -> Unit
    ) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observe<T>(sticky).collect {
                    block(it)
                }
            }
        }
    }

    fun clearAllStickyEvents() {
        SharedFlowEventBus.clearAllStickyEvents()
    }

}