package com.example.flowdownload.download.data

import com.example.flowdownload.download.model.Clock
import com.example.flowdownload.download.model.DownloadFailure
import com.example.flowdownload.download.model.DownloadFailureCategory
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadStatus
import com.example.flowdownload.download.model.PersistedDownload
import com.example.flowdownload.download.network.DownloadProgressSample
import com.example.flowdownload.download.network.DownloadStopController
import com.example.flowdownload.download.network.DownloadStopReason
import com.example.flowdownload.download.network.DownloadTerminalResult
import com.example.flowdownload.download.network.HttpDownloadEngine
import kotlinx.coroutines.CancellationException
import java.io.File

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载任务执行器，负责统一承载单条任务的状态迁移、网络执行、进度落库和终态收口。
 */
class DownloadTaskRunner(
    private val store: DownloadStore,
    private val engine: HttpDownloadEngine,
    private val clock: Clock,
) {
    suspend fun run(
        id: DownloadId,
        stopController: DownloadStopController,
    ): DownloadTaskRunResult {
        val record = store.get(id) ?: return DownloadTaskRunResult.Cancelled
        if (record.status is DownloadStatus.Cancelled || record.status is DownloadStatus.Success) {
            return DownloadTaskRunResult.Cancelled
        }

        if (record.retryCount > record.maxRetries) {
            val failure = DownloadFailure(
                category = DownloadFailureCategory.UNKNOWN,
                message = "Retry limit exceeded",
                retryable = false,
            )
            store.markFailed(
                id = id,
                failure = failure,
                bytesDownloaded = record.bytesDownloaded,
                totalBytes = record.totalBytes,
                eTag = record.eTag,
                lastModified = record.lastModified,
                now = clock.now(),
            )
            return DownloadTaskRunResult.Failed(failure)
        }

        val tempFile = File(record.tempDestination)
        if (!tempFile.exists() && record.bytesDownloaded > 0L) {
            store.markPaused(
                id = id,
                now = clock.now(),
                bytesDownloaded = 0L,
                totalBytes = record.totalBytes,
                eTag = record.eTag,
                lastModified = record.lastModified,
            )
        }

        store.markStarting(id, clock.now())

        val result = try {
            engine.run(
                record = store.get(id) ?: record,
                stopController = stopController,
                onProgress = { sample ->
                    persistProgress(id, sample)
                },
            )
        } catch (_: CancellationException) {
            resolveStoppedResult(
                id = id,
                initialRecord = record,
                stopController = stopController,
                tempFile = tempFile,
            )
        }

        return when (result) {
            is DownloadTerminalResult.Success -> {
                store.markSuccess(
                    id = id,
                    bytesDownloaded = result.bytesDownloaded,
                    totalBytes = result.totalBytes,
                    eTag = result.eTag,
                    lastModified = result.lastModified,
                    mimeType = result.mimeType,
                    now = clock.now(),
                )
                DownloadTaskRunResult.Success
            }

            is DownloadTerminalResult.Paused -> {
                store.markPaused(
                    id = id,
                    now = clock.now(),
                    bytesDownloaded = result.bytesDownloaded,
                    totalBytes = result.totalBytes,
                    eTag = result.eTag,
                    lastModified = result.lastModified,
                )
                DownloadTaskRunResult.Paused
            }

            is DownloadTerminalResult.Cancelled -> {
                if (result.deletePartialFile) {
                    File(record.tempDestination).delete()
                }
                store.markCancelled(id, clock.now())
                DownloadTaskRunResult.Cancelled
            }

            is DownloadTerminalResult.Failed -> {
                store.markFailed(
                    id = id,
                    failure = result.failure,
                    bytesDownloaded = result.bytesDownloaded,
                    totalBytes = result.totalBytes,
                    eTag = result.eTag,
                    lastModified = result.lastModified,
                    now = clock.now(),
                )
                DownloadTaskRunResult.Failed(result.failure)
            }
        }
    }

    private suspend fun resolveStoppedResult(
        id: DownloadId,
        initialRecord: PersistedDownload,
        stopController: DownloadStopController,
        tempFile: File,
    ): DownloadTerminalResult {
        val latestRecord = store.get(id) ?: initialRecord
        val pendingStopRequest = store.getStopRequest(id)
        val requestedStopReason = when {
            stopController.reason is DownloadStopReason.Pause -> PendingStopReason.PAUSE
            stopController.reason is DownloadStopReason.Cancel -> PendingStopReason.CANCEL
            else -> pendingStopRequest?.reason
        }
        val deletePartialFile = when (val reason = stopController.reason) {
            is DownloadStopReason.Cancel -> reason.deletePartialFile
            else -> pendingStopRequest?.deletePartialFile ?: false
        }

        return when (requestedStopReason) {
            PendingStopReason.CANCEL -> DownloadTerminalResult.Cancelled(
                bytesDownloaded = tempFile.length(),
                totalBytes = latestRecord.totalBytes,
                eTag = latestRecord.eTag,
                lastModified = latestRecord.lastModified,
                deletePartialFile = deletePartialFile,
            )

            PendingStopReason.PAUSE,
            null,
            -> DownloadTerminalResult.Paused(
                bytesDownloaded = tempFile.length(),
                totalBytes = latestRecord.totalBytes,
                eTag = latestRecord.eTag,
                lastModified = latestRecord.lastModified,
            )
        }
    }

    private suspend fun persistProgress(
        id: DownloadId,
        sample: DownloadProgressSample,
    ) {
        store.markRunning(
            id = id,
            bytesDownloaded = sample.bytesDownloaded,
            totalBytes = sample.totalBytes,
            bytesPerSecond = sample.bytesPerSecond,
            eTag = sample.eTag,
            lastModified = sample.lastModified,
            mimeType = sample.mimeType,
            now = sample.updatedAtEpochMs,
        )
    }
}

/**
 * 下载执行结果模型，供不同运行时承载层区分成功、暂停、取消和失败后的调度策略。
 */
sealed interface DownloadTaskRunResult {
    data object Success : DownloadTaskRunResult

    data object Paused : DownloadTaskRunResult

    data object Cancelled : DownloadTaskRunResult

    data class Failed(val failure: DownloadFailure) : DownloadTaskRunResult
}
