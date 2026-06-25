package com.example.flowdownload.download.model

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 持久化下载记录模型，封装数据库恢复、断点续传和调度所需的完整元数据。
 */
data class PersistedDownload(
    val id: DownloadId,
    val url: String,
    val destination: String,
    val tempDestination: String,
    val displayName: String?,
    val tag: String?,
    val group: String?,
    val extras: Map<String, String>,
    val headers: Map<String, String>,
    val constraints: DownloadConstraints,
    val expectedSha256: String?,
    val overwriteExisting: Boolean,
    val priority: Int,
    val status: DownloadStatus,
    val bytesDownloaded: Long,
    val totalBytes: Long?,
    val eTag: String?,
    val lastModified: String?,
    val mimeType: String?,
    val retryCount: Int,
    val maxRetries: Int,
    val nextAttemptAtEpochMs: Long?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

/** 标记记录是否仍处于活跃或可恢复状态。 */
val PersistedDownload.isActiveLike: Boolean
    get() = status.isActiveLike
