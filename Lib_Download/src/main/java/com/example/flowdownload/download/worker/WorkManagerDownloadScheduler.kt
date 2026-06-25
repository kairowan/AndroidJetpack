package com.example.flowdownload.download.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.flowdownload.FlowDownloadConfig
import com.example.flowdownload.download.data.DownloadRetryBackoffCalculator
import com.example.flowdownload.download.data.DownloadStore
import com.example.flowdownload.download.data.DownloadTaskScheduler
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadNetworkType
import com.example.flowdownload.download.model.DownloadStatus
import java.util.concurrent.TimeUnit

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 基于 WorkManager 的下载调度实现，负责把持久化任务投递到系统后台执行队列。
 */
class WorkManagerDownloadScheduler(
    context: Context,
    private val store: DownloadStore,
    private val config: FlowDownloadConfig,
) : DownloadTaskScheduler {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    override suspend fun resumePending() {
        store.getQueued().forEach { schedule(it.id) }
    }

    override suspend fun schedule(id: DownloadId) {
        val record = store.get(id) ?: return
        if (record.status !is DownloadStatus.Queued &&
            record.status !is DownloadStatus.RetryWaiting
        ) {
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(record.constraints.networkType.toWorkManagerNetworkType())
            .setRequiresCharging(record.constraints.requiresCharging)
            .setRequiresBatteryNotLow(record.constraints.requiresBatteryNotLow)
            .setRequiresStorageNotLow(record.constraints.requiresStorageNotLow)
            .setRequiresDeviceIdle(record.constraints.requiresDeviceIdle)
            .build()
        val initialDelayMillis = DownloadRetryBackoffCalculator.remainingDelayMillis(
            now = System.currentTimeMillis(),
            nextAttemptAtEpochMs = record.nextAttemptAtEpochMs,
        )
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(DownloadWorkDataCodec.encode(id, config))
            .setConstraints(constraints)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                config.retryBackoffMillis.coerceAtLeast(WorkRequest.MIN_BACKOFF_MILLIS),
                TimeUnit.MILLISECONDS,
            )
            .addTag(id.value)
            .build()

        workManager.enqueueUniqueWork(
            DownloadWorkDataCodec.uniqueWorkName(id),
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    override suspend fun pause(id: DownloadId) {
        workManager.cancelUniqueWork(DownloadWorkDataCodec.uniqueWorkName(id))
    }

    override suspend fun cancel(id: DownloadId, deletePartialFile: Boolean) {
        workManager.cancelUniqueWork(DownloadWorkDataCodec.uniqueWorkName(id))
    }

    private fun DownloadNetworkType.toWorkManagerNetworkType(): NetworkType {
        return when (this) {
            DownloadNetworkType.CONNECTED -> NetworkType.CONNECTED
            DownloadNetworkType.UNMETERED -> NetworkType.UNMETERED
            DownloadNetworkType.NOT_ROAMING -> NetworkType.NOT_ROAMING
            DownloadNetworkType.METERED -> NetworkType.METERED
        }
    }
}
