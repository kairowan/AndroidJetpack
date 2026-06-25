package com.example.flowdownload.download.model

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载状态模型，对外明确区分排队、运行、暂停、成功、取消和失败等阶段。
 */
sealed interface DownloadStatus {
    data object Queued : DownloadStatus

    data class RetryWaiting(
        val failure: DownloadFailure,
        val nextAttemptAtEpochMs: Long,
    ) : DownloadStatus

    data object Starting : DownloadStatus

    data class Running(val bytesPerSecond: Long?) : DownloadStatus

    data object Paused : DownloadStatus

    data object Success : DownloadStatus

    data object Cancelled : DownloadStatus

    data class Failed(val failure: DownloadFailure) : DownloadStatus
}

/**
 * 当前状态是否属于终态。
 */
val DownloadStatus.isTerminal: Boolean
    get() = this is DownloadStatus.Success ||
        this is DownloadStatus.Cancelled ||
        this is DownloadStatus.Failed

/**
 * 当前状态是否仍然属于活跃队列。
 */
val DownloadStatus.isActiveLike: Boolean
    get() = this is DownloadStatus.Queued ||
        this is DownloadStatus.RetryWaiting ||
        this is DownloadStatus.Starting ||
        this is DownloadStatus.Running ||
        this is DownloadStatus.Paused

/**
 * 当前状态是否可以继续执行。
 */
val DownloadStatus.canResume: Boolean
    get() = this is DownloadStatus.Paused

/**
 * 当前状态是否可以重新发起重试。
 */
val DownloadStatus.canRetry: Boolean
    get() = this is DownloadStatus.Failed && failure.retryable

/**
 * 当前状态是否可以暂停。
 */
val DownloadStatus.canPause: Boolean
    get() = this is DownloadStatus.Queued ||
        this is DownloadStatus.RetryWaiting ||
        this is DownloadStatus.Starting ||
        this is DownloadStatus.Running

/**
 * 当前状态是否可以取消。
 */
val DownloadStatus.canCancel: Boolean
    get() = !isTerminal
