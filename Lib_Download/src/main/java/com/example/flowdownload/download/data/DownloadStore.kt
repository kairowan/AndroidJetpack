package com.example.flowdownload.download.data

import com.example.flowdownload.download.model.DownloadFailure
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadRequest
import com.example.flowdownload.download.model.DownloadSnapshot
import com.example.flowdownload.download.model.PersistedDownload
import kotlinx.coroutines.flow.Flow

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载存储抽象，负责下载状态读写、恢复以及终态记录清理。
 */
interface DownloadStore {
    fun observe(id: DownloadId): Flow<DownloadSnapshot?>

    fun observeAll(): Flow<List<DownloadSnapshot>>

    fun observeByUrl(url: String): Flow<List<DownloadSnapshot>>

    fun observeByDestination(destination: String): Flow<List<DownloadSnapshot>>

    fun observeByTag(tag: String): Flow<List<DownloadSnapshot>>

    fun observeByGroup(group: String): Flow<List<DownloadSnapshot>>

    suspend fun insertQueued(request: DownloadRequest, now: Long)

    suspend fun get(id: DownloadId): PersistedDownload?

    suspend fun getAll(): List<PersistedDownload>

    suspend fun getByUrl(url: String): List<PersistedDownload>

    suspend fun getByDestination(destination: String): List<PersistedDownload>

    suspend fun getByTag(tag: String): List<PersistedDownload>

    suspend fun getByGroup(group: String): List<PersistedDownload>

    suspend fun getActive(): List<PersistedDownload>

    suspend fun getQueued(): List<PersistedDownload>

    suspend fun getTerminal(): List<PersistedDownload>

    suspend fun findActiveByDestination(destination: String): PersistedDownload?

    suspend fun updatePriority(id: DownloadId, priority: Int, now: Long)

    suspend fun getStopRequest(id: DownloadId): PendingStopRequest?

    suspend fun markPauseRequested(id: DownloadId, now: Long)

    suspend fun markCancelRequested(id: DownloadId, deletePartialFile: Boolean, now: Long)

    suspend fun markQueued(id: DownloadId, now: Long, resetFailure: Boolean = false)

    suspend fun markRetryWaiting(
        id: DownloadId,
        failure: DownloadFailure,
        nextAttemptAtEpochMs: Long,
        now: Long,
    )

    suspend fun markStarting(id: DownloadId, now: Long)

    suspend fun markRunning(
        id: DownloadId,
        bytesDownloaded: Long,
        totalBytes: Long?,
        bytesPerSecond: Long?,
        eTag: String?,
        lastModified: String?,
        mimeType: String?,
        now: Long,
    )

    suspend fun markPaused(
        id: DownloadId,
        now: Long,
        bytesDownloaded: Long? = null,
        totalBytes: Long? = null,
        eTag: String? = null,
        lastModified: String? = null,
    )

    suspend fun markCancelled(id: DownloadId, now: Long)

    suspend fun markSuccess(
        id: DownloadId,
        bytesDownloaded: Long,
        totalBytes: Long?,
        eTag: String?,
        lastModified: String?,
        mimeType: String?,
        now: Long,
    )

    suspend fun markFailed(
        id: DownloadId,
        failure: DownloadFailure,
        bytesDownloaded: Long,
        totalBytes: Long?,
        eTag: String?,
        lastModified: String?,
        now: Long,
    )

    suspend fun incrementRetryCount(id: DownloadId, now: Long)

    suspend fun recoverInterrupted(now: Long)

    suspend fun delete(id: DownloadId)
}
