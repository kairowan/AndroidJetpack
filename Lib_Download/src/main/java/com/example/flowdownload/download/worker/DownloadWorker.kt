package com.example.flowdownload.download.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.flowdownload.FlowDownloadComponents
import com.example.flowdownload.FlowDownload
import com.example.flowdownload.download.data.DownloadRetryBackoffCalculator
import com.example.flowdownload.download.data.DownloadMapper
import com.example.flowdownload.download.data.DownloadTaskRunResult
import com.example.flowdownload.download.data.DownloadTaskRunner
import com.example.flowdownload.download.model.DownloadSnapshot
import com.example.flowdownload.download.network.DownloadStopController
import com.example.flowdownload.download.network.DownloadStopReason
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载后台 Worker，负责在进程回收或界面退出后继续执行持久化下载任务。
 */
class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        val downloadId = DownloadWorkDataCodec.decodeDownloadId(inputData)
        val config = DownloadWorkDataCodec.decodeConfig(inputData)
        FlowDownload.rememberConfig(config)
        val components = FlowDownloadComponents.create(applicationContext, config)
        val notifier = DownloadWorkerNotifier(applicationContext, config.notification)
        val runner = DownloadTaskRunner(
            store = components.store,
            engine = components.engine,
            clock = components.clock,
        )

        var observeJob: Job? = null
        try {
            if (config.notification.enabled) {
                notifier.ensureChannel()
                components.store.get(downloadId)?.let { record ->
                    setForeground(
                        notifier.createForegroundInfo(DownloadMapper.persistedToSnapshot(record)),
                    )
                }
                observeJob = launch {
                    components.store.observe(downloadId)
                        .filterNotNull()
                        .collect { snapshot ->
                            setForeground(notifier.createForegroundInfo(snapshot))
                        }
                }
            }

            when (val outcome = runner.run(downloadId, WorkerStopController)) {
                DownloadTaskRunResult.Success -> {
                    emitTerminalNotification(components.store.observe(downloadId).firstOrNull(), notifier)
                    Result.success()
                }

                DownloadTaskRunResult.Paused -> {
                    notifier.cancel(downloadId)
                    Result.success()
                }

                DownloadTaskRunResult.Cancelled -> {
                    notifier.cancel(downloadId)
                    Result.success()
                }

                is DownloadTaskRunResult.Failed -> {
                    val record = components.store.get(downloadId)
                    val canRetry = record != null &&
                        outcome.failure.retryable &&
                        record.retryCount < record.maxRetries

                    if (canRetry) {
                        val now = components.clock.now()
                        val nextAttemptAt = DownloadRetryBackoffCalculator.nextAttemptAtEpochMs(
                            now = now,
                            retryCount = record.retryCount,
                            baseBackoffMillis = config.retryBackoffMillis,
                        )
                        components.store.incrementRetryCount(downloadId, now)
                        components.store.markRetryWaiting(
                            id = downloadId,
                            failure = outcome.failure,
                            nextAttemptAtEpochMs = nextAttemptAt,
                            now = now,
                        )
                        notifier.cancel(downloadId)
                        Result.retry()
                    } else {
                        emitTerminalNotification(
                            components.store.observe(downloadId).firstOrNull(),
                            notifier,
                        )
                        Result.failure()
                    }
                }
            }
        } finally {
            observeJob?.cancel()
            components.database.close()
        }
    }

    private fun emitTerminalNotification(
        snapshot: DownloadSnapshot?,
        notifier: DownloadWorkerNotifier,
    ) {
        if (snapshot != null) {
            notifier.showTerminal(snapshot)
        }
    }

    private object WorkerStopController : DownloadStopController {
        override val reason: DownloadStopReason? = null
    }
}
