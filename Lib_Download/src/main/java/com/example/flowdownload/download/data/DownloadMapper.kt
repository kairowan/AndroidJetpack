package com.example.flowdownload.download.data

import com.example.flowdownload.download.data.local.DownloadEntity
import com.example.flowdownload.download.model.DownloadFailure
import com.example.flowdownload.download.model.DownloadFailureCategory
import com.example.flowdownload.download.model.DownloadConstraints
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadNetworkType
import com.example.flowdownload.download.model.DownloadRequest
import com.example.flowdownload.download.model.DownloadSnapshot
import com.example.flowdownload.download.model.DownloadStatus
import com.example.flowdownload.download.model.PersistedDownload

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载领域模型和数据库实体之间的映射器，统一处理状态和进度转换。
 */
object DownloadMapper {
    fun requestToEntity(
        request: DownloadRequest,
        now: Long,
    ): DownloadEntity {
        val destination = DownloadPathResolver.canonicalize(request.destination)
        return DownloadEntity(
            id = request.id.value,
            url = request.url,
            destination = destination,
            tempDestination = DownloadPathResolver.tempPathFor(
                destination = destination,
                explicitTempFilePath = request.tempFilePath,
            ),
            displayName = DownloadPathResolver.displayNameFor(destination, request.displayName),
            tag = request.tag,
            groupName = request.group,
            extrasBlob = StringMapCodec.encode(request.extras),
            networkType = request.constraints.networkType.name,
            requiresCharging = request.constraints.requiresCharging,
            requiresBatteryNotLow = request.constraints.requiresBatteryNotLow,
            requiresStorageNotLow = request.constraints.requiresStorageNotLow,
            requiresDeviceIdle = request.constraints.requiresDeviceIdle,
            priority = request.priority,
            status = DownloadStatuses.QUEUED,
            bytesDownloaded = 0L,
            totalBytes = null,
            eTag = null,
            lastModified = null,
            mimeType = null,
            errorCategory = null,
            errorMessage = null,
            retryable = false,
            retryCount = 0,
            maxRetries = request.maxRetries,
            nextAttemptAtEpochMs = null,
            headersBlob = HeaderCodec.encode(request.headers),
            expectedSha256 = request.expectedSha256,
            overwriteExisting = request.overwriteExisting,
            pendingStopReason = null,
            deletePartialOnCancel = false,
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
            bytesPerSecond = null,
        )
    }

    fun entityToPersisted(entity: DownloadEntity): PersistedDownload = PersistedDownload(
        id = DownloadId(entity.id),
        url = entity.url,
        destination = entity.destination,
        tempDestination = entity.tempDestination,
        displayName = entity.displayName,
        tag = entity.tag,
        group = entity.groupName,
        extras = StringMapCodec.decode(entity.extrasBlob),
        headers = HeaderCodec.decode(entity.headersBlob),
        constraints = entity.toConstraints(),
        expectedSha256 = entity.expectedSha256,
        overwriteExisting = entity.overwriteExisting,
        priority = entity.priority,
        status = entity.toStatus(),
        bytesDownloaded = entity.bytesDownloaded,
        totalBytes = entity.totalBytes,
        eTag = entity.eTag,
        lastModified = entity.lastModified,
        mimeType = entity.mimeType,
        retryCount = entity.retryCount,
        maxRetries = entity.maxRetries,
        nextAttemptAtEpochMs = entity.nextAttemptAtEpochMs,
        createdAtEpochMs = entity.createdAtEpochMs,
        updatedAtEpochMs = entity.updatedAtEpochMs,
    )

    fun entityToSnapshot(entity: DownloadEntity): DownloadSnapshot = DownloadSnapshot(
        id = DownloadId(entity.id),
        url = entity.url,
        destination = entity.destination,
        displayName = entity.displayName,
        tag = entity.tag,
        group = entity.groupName,
        extras = StringMapCodec.decode(entity.extrasBlob),
        status = entity.toStatus(),
        bytesDownloaded = entity.bytesDownloaded,
        totalBytes = entity.totalBytes,
        progressPercent = progressPercent(
            bytesDownloaded = entity.bytesDownloaded,
            totalBytes = entity.totalBytes,
        ),
        eTag = entity.eTag,
        lastModified = entity.lastModified,
        constraints = entity.toConstraints(),
        retryCount = entity.retryCount,
        maxRetries = entity.maxRetries,
        priority = entity.priority,
        nextAttemptAtEpochMs = entity.nextAttemptAtEpochMs,
        updatedAtEpochMs = entity.updatedAtEpochMs,
        mimeType = entity.mimeType,
        expectedSha256 = entity.expectedSha256,
        overwriteExisting = entity.overwriteExisting,
        createdAtEpochMs = entity.createdAtEpochMs,
    )

    fun persistedToSnapshot(record: PersistedDownload): DownloadSnapshot = DownloadSnapshot(
        id = record.id,
        url = record.url,
        destination = record.destination,
        displayName = record.displayName,
        tag = record.tag,
        group = record.group,
        extras = record.extras,
        status = record.status,
        bytesDownloaded = record.bytesDownloaded,
        totalBytes = record.totalBytes,
        progressPercent = progressPercent(
            bytesDownloaded = record.bytesDownloaded,
            totalBytes = record.totalBytes,
        ),
        eTag = record.eTag,
        lastModified = record.lastModified,
        constraints = record.constraints,
        retryCount = record.retryCount,
        maxRetries = record.maxRetries,
        priority = record.priority,
        nextAttemptAtEpochMs = record.nextAttemptAtEpochMs,
        updatedAtEpochMs = record.updatedAtEpochMs,
        mimeType = record.mimeType,
        expectedSha256 = record.expectedSha256,
        overwriteExisting = record.overwriteExisting,
        createdAtEpochMs = record.createdAtEpochMs,
    )

    fun progressPercent(bytesDownloaded: Long, totalBytes: Long?): Int? {
        if (totalBytes == null || totalBytes <= 0L) return null
        return ((bytesDownloaded.coerceAtMost(totalBytes) * 100) / totalBytes).toInt()
    }

    private fun DownloadEntity.toStatus(): DownloadStatus {
        return when (status) {
            DownloadStatuses.QUEUED -> DownloadStatus.Queued
            DownloadStatuses.RETRY_WAITING -> DownloadStatus.RetryWaiting(
                failure = DownloadFailure(
                    category = errorCategory?.let(DownloadFailureCategory::valueOf)
                        ?: DownloadFailureCategory.UNKNOWN,
                    message = errorMessage ?: "Waiting for retry",
                    retryable = retryable,
                ),
                nextAttemptAtEpochMs = nextAttemptAtEpochMs ?: updatedAtEpochMs,
            )
            DownloadStatuses.STARTING -> DownloadStatus.Starting
            DownloadStatuses.RUNNING -> DownloadStatus.Running(bytesPerSecond = bytesPerSecond)
            DownloadStatuses.PAUSED -> DownloadStatus.Paused
            DownloadStatuses.SUCCESS -> DownloadStatus.Success
            DownloadStatuses.CANCELLED -> DownloadStatus.Cancelled
            DownloadStatuses.FAILED -> DownloadStatus.Failed(
                failure = DownloadFailure(
                    category = errorCategory?.let(DownloadFailureCategory::valueOf)
                        ?: DownloadFailureCategory.UNKNOWN,
                    message = errorMessage ?: "Download failed",
                    retryable = retryable,
                ),
            )

            else -> DownloadStatus.Failed(
                failure = DownloadFailure(
                    category = DownloadFailureCategory.UNKNOWN,
                    message = "Unsupported status $status",
                    retryable = false,
                ),
            )
        }
    }

    private fun DownloadEntity.toConstraints(): DownloadConstraints {
        return DownloadConstraints(
            networkType = runCatching { DownloadNetworkType.valueOf(networkType) }
                .getOrDefault(DownloadNetworkType.CONNECTED),
            requiresCharging = requiresCharging,
            requiresBatteryNotLow = requiresBatteryNotLow,
            requiresStorageNotLow = requiresStorageNotLow,
            requiresDeviceIdle = requiresDeviceIdle,
        )
    }
}

/**
 * 数据库存储状态常量，统一约束 DAO 查询和实体持久化使用的状态值。
 */
object DownloadStatuses {
    const val QUEUED = "queued"
    const val RETRY_WAITING = "retry_waiting"
    const val STARTING = "starting"
    const val RUNNING = "running"
    const val PAUSED = "paused"
    const val SUCCESS = "success"
    const val CANCELLED = "cancelled"
    const val FAILED = "failed"

    val activeStatuses = listOf(QUEUED, RETRY_WAITING, STARTING, RUNNING, PAUSED)
    val interruptedStatuses = listOf(STARTING, RUNNING)
    val queuedStatuses = listOf(QUEUED, RETRY_WAITING)
    val terminalStatuses = listOf(SUCCESS, CANCELLED, FAILED)
}
