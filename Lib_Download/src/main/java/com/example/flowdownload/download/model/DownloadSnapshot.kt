package com.example.flowdownload.download.model

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载快照模型，作为 Flow 对外发射的统一只读状态，供宿主界面或日志系统消费。
 */
data class DownloadSnapshot(
    val id: DownloadId,
    val url: String,
    val destination: String,
    val displayName: String?,
    val tag: String?,
    val group: String?,
    val extras: Map<String, String>,
    val status: DownloadStatus,
    val bytesDownloaded: Long,
    val totalBytes: Long?,
    val progressPercent: Int?,
    val eTag: String? = null,
    val lastModified: String? = null,
    val constraints: DownloadConstraints = DownloadConstraints(),
    val retryCount: Int,
    val maxRetries: Int,
    val priority: Int,
    val nextAttemptAtEpochMs: Long?,
    val updatedAtEpochMs: Long,
    val mimeType: String? = null,
    val expectedSha256: String? = null,
    val overwriteExisting: Boolean = false,
    val createdAtEpochMs: Long = 0L,
) {
    val isTerminal: Boolean
        get() = status.isTerminal

    val isActiveLike: Boolean
        get() = status.isActiveLike

    val canPause: Boolean
        get() = status.canPause

    val canResume: Boolean
        get() = status.canResume

    val canCancel: Boolean
        get() = status.canCancel

    val canRetry: Boolean
        get() = status.canRetry && retryCount < maxRetries

    val remainingRetries: Int
        get() = (maxRetries - retryCount).coerceAtLeast(0)

    val progressFraction: Float?
        get() = progressPercent?.div(100f)
}
