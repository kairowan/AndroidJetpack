package com.example.flowdownload.download.model

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 详细入队结果模型，帮助宿主区分本次是创建了新任务、复用了已有任务还是替换了旧任务。
 */
sealed interface DownloadEnqueueResult {
    val request: DownloadRequest
    val downloadId: DownloadId

    data class Created(
        override val request: DownloadRequest,
        override val downloadId: DownloadId,
    ) : DownloadEnqueueResult

    data class ReusedExisting(
        override val request: DownloadRequest,
        val existingSnapshot: DownloadSnapshot,
    ) : DownloadEnqueueResult {
        override val downloadId: DownloadId
            get() = existingSnapshot.id
    }

    data class ReplacedExisting(
        override val request: DownloadRequest,
        val replacedSnapshot: DownloadSnapshot,
        override val downloadId: DownloadId,
    ) : DownloadEnqueueResult {
        val replacedDownloadId: DownloadId
            get() = replacedSnapshot.id
    }
}
