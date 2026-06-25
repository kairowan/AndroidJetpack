package com.example.flowdownload.download.data

import com.example.flowdownload.download.data.local.DownloadDao
import com.example.flowdownload.download.data.local.DownloadEntity
import com.example.flowdownload.download.model.DownloadFailure
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadRequest
import com.example.flowdownload.download.model.DownloadSnapshot
import com.example.flowdownload.download.model.PersistedDownload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 基于 Room 的下载存储实现，负责序列化状态迁移并做启动期恢复修正。
 */
class RoomDownloadStore(
    private val dao: DownloadDao,
) : DownloadStore {
    private val writeMutex = Mutex()

    override fun observe(id: DownloadId): Flow<DownloadSnapshot?> =
        dao.observe(id.value).map { entity ->
            entity?.let(DownloadMapper::entityToSnapshot)
        }

    override fun observeAll(): Flow<List<DownloadSnapshot>> =
        dao.observeAll().map { entities ->
            entities.map(DownloadMapper::entityToSnapshot)
        }

    override fun observeByUrl(url: String): Flow<List<DownloadSnapshot>> =
        dao.observeByUrl(url).map { entities ->
            entities.map(DownloadMapper::entityToSnapshot)
        }

    override fun observeByDestination(destination: String): Flow<List<DownloadSnapshot>> =
        dao.observeByDestination(destination).map { entities ->
            entities.map(DownloadMapper::entityToSnapshot)
        }

    override fun observeByTag(tag: String): Flow<List<DownloadSnapshot>> =
        dao.observeByTag(tag).map { entities ->
            entities.map(DownloadMapper::entityToSnapshot)
        }

    override fun observeByGroup(group: String): Flow<List<DownloadSnapshot>> =
        dao.observeByGroup(group).map { entities ->
            entities.map(DownloadMapper::entityToSnapshot)
        }

    override suspend fun insertQueued(request: DownloadRequest, now: Long) {
        dao.upsert(DownloadMapper.requestToEntity(request, now))
    }

    override suspend fun get(id: DownloadId): PersistedDownload? =
        dao.get(id.value)?.let(DownloadMapper::entityToPersisted)

    override suspend fun getAll(): List<PersistedDownload> =
        dao.getAll().map(DownloadMapper::entityToPersisted)

    override suspend fun getByUrl(url: String): List<PersistedDownload> =
        dao.getByUrl(url).map(DownloadMapper::entityToPersisted)

    override suspend fun getByDestination(destination: String): List<PersistedDownload> =
        dao.getByDestination(destination).map(DownloadMapper::entityToPersisted)

    override suspend fun getByTag(tag: String): List<PersistedDownload> =
        dao.getByTag(tag).map(DownloadMapper::entityToPersisted)

    override suspend fun getByGroup(group: String): List<PersistedDownload> =
        dao.getByGroup(group).map(DownloadMapper::entityToPersisted)

    override suspend fun getActive(): List<PersistedDownload> =
        dao.getByStatuses(DownloadStatuses.activeStatuses).map(DownloadMapper::entityToPersisted)

    override suspend fun getQueued(): List<PersistedDownload> =
        dao.getByStatuses(DownloadStatuses.queuedStatuses).map(DownloadMapper::entityToPersisted)

    override suspend fun getTerminal(): List<PersistedDownload> =
        dao.getByStatuses(DownloadStatuses.terminalStatuses).map(DownloadMapper::entityToPersisted)

    override suspend fun findActiveByDestination(destination: String): PersistedDownload? =
        dao.findByDestination(
            destination = destination,
            statuses = DownloadStatuses.activeStatuses,
        )?.let(DownloadMapper::entityToPersisted)

    override suspend fun updatePriority(id: DownloadId, priority: Int, now: Long) {
        writeMutex.withLock {
            dao.updatePriority(
                id = id.value,
                priority = priority,
                now = now,
            )
        }
    }

    override suspend fun getStopRequest(id: DownloadId): PendingStopRequest? =
        dao.get(id.value)?.let { entity ->
            val reason = entity.pendingStopReason?.let(PendingStopReason::valueOf) ?: return null
            PendingStopRequest(
                reason = reason,
                deletePartialFile = entity.deletePartialOnCancel,
            )
        }

    override suspend fun markPauseRequested(id: DownloadId, now: Long) {
        update(id) { current ->
            current.copy(
                pendingStopReason = PendingStopReason.PAUSE.name,
                deletePartialOnCancel = false,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun markCancelRequested(id: DownloadId, deletePartialFile: Boolean, now: Long) {
        update(id) { current ->
            current.copy(
                pendingStopReason = PendingStopReason.CANCEL.name,
                deletePartialOnCancel = deletePartialFile,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun markQueued(id: DownloadId, now: Long, resetFailure: Boolean) {
        update(id) { current ->
            current.copy(
                status = DownloadStatuses.QUEUED,
                errorCategory = if (resetFailure) null else current.errorCategory,
                errorMessage = if (resetFailure) null else current.errorMessage,
                retryable = if (resetFailure) false else current.retryable,
                nextAttemptAtEpochMs = null,
                pendingStopReason = null,
                deletePartialOnCancel = false,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun markRetryWaiting(
        id: DownloadId,
        failure: DownloadFailure,
        nextAttemptAtEpochMs: Long,
        now: Long,
    ) {
        update(id) { current ->
            current.copy(
                status = DownloadStatuses.RETRY_WAITING,
                errorCategory = failure.category.name,
                errorMessage = failure.message,
                retryable = failure.retryable,
                nextAttemptAtEpochMs = nextAttemptAtEpochMs,
                bytesPerSecond = null,
                pendingStopReason = null,
                deletePartialOnCancel = false,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun markStarting(id: DownloadId, now: Long) {
        update(id) { current ->
            current.copy(
                status = DownloadStatuses.STARTING,
                errorCategory = null,
                errorMessage = null,
                retryable = false,
                nextAttemptAtEpochMs = null,
                pendingStopReason = null,
                deletePartialOnCancel = false,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun markRunning(
        id: DownloadId,
        bytesDownloaded: Long,
        totalBytes: Long?,
        bytesPerSecond: Long?,
        eTag: String?,
        lastModified: String?,
        mimeType: String?,
        now: Long,
    ) {
        update(id) { current ->
            current.copy(
                status = DownloadStatuses.RUNNING,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes,
                bytesPerSecond = bytesPerSecond,
                eTag = eTag ?: current.eTag,
                lastModified = lastModified ?: current.lastModified,
                mimeType = mimeType ?: current.mimeType,
                nextAttemptAtEpochMs = null,
                pendingStopReason = null,
                deletePartialOnCancel = false,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun markPaused(
        id: DownloadId,
        now: Long,
        bytesDownloaded: Long?,
        totalBytes: Long?,
        eTag: String?,
        lastModified: String?,
    ) {
        update(id) { current ->
            current.copy(
                status = DownloadStatuses.PAUSED,
                bytesDownloaded = bytesDownloaded ?: current.bytesDownloaded,
                totalBytes = totalBytes ?: current.totalBytes,
                eTag = eTag ?: current.eTag,
                lastModified = lastModified ?: current.lastModified,
                bytesPerSecond = null,
                nextAttemptAtEpochMs = null,
                pendingStopReason = null,
                deletePartialOnCancel = false,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun markCancelled(id: DownloadId, now: Long) {
        update(id) { current ->
            current.copy(
                status = DownloadStatuses.CANCELLED,
                bytesPerSecond = null,
                nextAttemptAtEpochMs = null,
                pendingStopReason = null,
                deletePartialOnCancel = false,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun markSuccess(
        id: DownloadId,
        bytesDownloaded: Long,
        totalBytes: Long?,
        eTag: String?,
        lastModified: String?,
        mimeType: String?,
        now: Long,
    ) {
        update(id) { current ->
            current.copy(
                status = DownloadStatuses.SUCCESS,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes ?: current.totalBytes,
                eTag = eTag ?: current.eTag,
                lastModified = lastModified ?: current.lastModified,
                mimeType = mimeType ?: current.mimeType,
                errorCategory = null,
                errorMessage = null,
                retryable = false,
                bytesPerSecond = null,
                nextAttemptAtEpochMs = null,
                pendingStopReason = null,
                deletePartialOnCancel = false,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun markFailed(
        id: DownloadId,
        failure: DownloadFailure,
        bytesDownloaded: Long,
        totalBytes: Long?,
        eTag: String?,
        lastModified: String?,
        now: Long,
    ) {
        update(id) { current ->
            current.copy(
                status = DownloadStatuses.FAILED,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes ?: current.totalBytes,
                eTag = eTag ?: current.eTag,
                lastModified = lastModified ?: current.lastModified,
                errorCategory = failure.category.name,
                errorMessage = failure.message,
                retryable = failure.retryable,
                bytesPerSecond = null,
                nextAttemptAtEpochMs = null,
                pendingStopReason = null,
                deletePartialOnCancel = false,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun incrementRetryCount(id: DownloadId, now: Long) {
        update(id) { current ->
            current.copy(
                retryCount = current.retryCount + 1,
                updatedAtEpochMs = now,
            )
        }
    }

    override suspend fun recoverInterrupted(now: Long) {
        writeMutex.withLock {
            dao.getByStatuses(DownloadStatuses.interruptedStatuses)
                .forEach { entity ->
                    val tempFile = File(entity.tempDestination)
                    val reconciledBytes = tempFile.takeIf(File::exists)?.length() ?: 0L
                    dao.upsert(
                        entity.copy(
                            status = DownloadStatuses.PAUSED,
                            bytesDownloaded = reconciledBytes,
                            bytesPerSecond = null,
                            nextAttemptAtEpochMs = null,
                            pendingStopReason = null,
                            deletePartialOnCancel = false,
                            updatedAtEpochMs = now,
                        ),
                    )
                }
        }
    }

    override suspend fun delete(id: DownloadId) {
        dao.deleteById(id.value)
    }

    private suspend fun update(
        id: DownloadId,
        transform: (DownloadEntity) -> DownloadEntity,
    ) {
        writeMutex.withLock {
            val current = dao.get(id.value) ?: return
            dao.upsert(transform(current))
        }
    }
}
