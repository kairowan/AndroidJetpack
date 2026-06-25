package com.ghn.lib.base.web.bridge

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 浩楠
 *
 * @date 2026/6/24
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: WebBridge 订阅管理工具，负责维护长连接回调的订阅关系。
 */

object WebBridgeSubscriptions {
    private val subscriptions = ConcurrentHashMap<String, Job>()

    fun <T : Any> subscribe(
        host: WebBridgeHost,
        channel: String,
        taskId: String,
        callbackHandler: String,
        snapshots: Flow<T?>,
        payloadMapper: (T) -> Map<String, Any?>,
    ): String {
        val key = subscriptionKey(
            activity = host.activity,
            channel = channel,
            taskId = taskId,
            callbackHandler = callbackHandler
        )
        subscriptions.remove(key)?.cancel()
        val job = host.activity.lifecycleScope.launch {
            snapshots
                .filterNotNull()
                .distinctUntilChanged()
                .collect { snapshot ->
                    host.callHandler(
                        handlerName = callbackHandler,
                        data = payloadMapper(snapshot).toWebBridgeJson()
                    )
                }
        }
        subscriptions[key] = job
        job.invokeOnCompletion {
            subscriptions.remove(key, job)
        }
        return key
    }

    fun unsubscribe(
        host: WebBridgeHost,
        channel: String,
        taskId: String,
        callbackHandler: String,
    ): Boolean {
        val key = subscriptionKey(
            activity = host.activity,
            channel = channel,
            taskId = taskId,
            callbackHandler = callbackHandler
        )
        val job = subscriptions.remove(key) ?: return false
        job.cancel()
        return true
    }
}

private fun subscriptionKey(
    activity: FragmentActivity,
    channel: String,
    taskId: String,
    callbackHandler: String,
): String {
    return buildString {
        append(System.identityHashCode(activity))
        append(':')
        append(channel)
        append(':')
        append(taskId)
        append(':')
        append(callbackHandler)
    }
}
