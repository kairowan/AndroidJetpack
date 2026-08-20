package com.kotlinmvvm.app.observability

import android.util.Log
import com.kotlinmvvm.core.network.observer.NetworkFailureObserver
import com.kotlinmvvm.core.network.result.NetworkFailure
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskEvent
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskException
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver
import com.kotlinmvvm.data.feed.observer.FeedCacheOperation
import com.kotlinmvvm.data.feed.observer.FeedDataObserver
import com.kotlinmvvm.domain.feed.model.FeedSource

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用诊断汇聚器，统一接收任务、网络与缓存信号并为商业监控平台保留单一替换点
 */
internal class AppDiagnosticObserver(
    private val tag: String,
    private val taskMessageFormat: String,
    private val networkMessageFormat: String,
    private val cacheMessageFormat: String,
    private val logTaskLifecycle: Boolean,
    private val logFailureCauses: Boolean
) : ViewModelTaskObserver, NetworkFailureObserver, FeedDataObserver {

    override fun onTaskEvent(
        taskKey: String,
        event: ViewModelTaskEvent,
        exception: ViewModelTaskException?
    ) {
        val message = taskMessageFormat.format(taskKey, event.diagnosticCode)
        if (exception != null) {
            logWarning(message, exception.originalException)
        } else if (logTaskLifecycle) {
            Log.d(tag, message)
        }
    }

    override fun onFailure(failure: NetworkFailure, cause: Throwable?) {
        logWarning(networkMessageFormat.format(failure.diagnosticCode, failure.retryable), cause)
    }

    override fun onCacheFailure(
        source: FeedSource,
        operation: FeedCacheOperation,
        error: Throwable
    ) {
        logWarning(
            cacheMessageFormat.format(source.diagnosticCode, operation.diagnosticCode),
            error
        )
    }

    private fun logWarning(message: String, cause: Throwable?) {
        if (logFailureCauses && cause != null) Log.w(tag, message, cause) else Log.w(tag, message)
    }
}
