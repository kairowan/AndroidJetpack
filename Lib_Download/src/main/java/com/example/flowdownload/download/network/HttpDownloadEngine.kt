package com.example.flowdownload.download.network

import com.example.flowdownload.download.model.DownloadFailure
import com.example.flowdownload.download.model.PersistedDownload

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载网络引擎抽象，负责执行真实的 HTTP 流式传输并返回统一终态结果。
 */
interface HttpDownloadEngine {
    suspend fun run(
        record: PersistedDownload,
        stopController: DownloadStopController,
        onProgress: suspend (DownloadProgressSample) -> Unit,
    ): DownloadTerminalResult
}

/**
 * 下载停止信号抽象，供调度层向网络层传递暂停或取消的协作式中断语义。
 */
interface DownloadStopController {
    val reason: DownloadStopReason?
}

/**
 *下载进度采样值，用于跨网络层和存储层传递统一的进度元信息。
 */
data class DownloadProgressSample(
    val bytesDownloaded: Long,
    val totalBytes: Long?,
    val bytesPerSecond: Long?,
    val updatedAtEpochMs: Long,
    val eTag: String?,
    val lastModified: String?,
    val mimeType: String?,
)

/**
 * 下载停止原因模型，对网络引擎显式区分暂停和取消行为。
 */
sealed interface DownloadStopReason {
    data object Pause : DownloadStopReason

    data class Cancel(val deletePartialFile: Boolean) : DownloadStopReason
}

/**
 * 下载终态结果模型，统一表达成功、暂停、取消和失败后的输出信息。
 */
sealed interface DownloadTerminalResult {
    data class Success(
        val bytesDownloaded: Long,
        val totalBytes: Long?,
        val eTag: String?,
        val lastModified: String?,
        val mimeType: String?,
    ) : DownloadTerminalResult

    data class Paused(
        val bytesDownloaded: Long,
        val totalBytes: Long?,
        val eTag: String?,
        val lastModified: String?,
    ) : DownloadTerminalResult

    data class Cancelled(
        val bytesDownloaded: Long,
        val totalBytes: Long?,
        val eTag: String?,
        val lastModified: String?,
        val deletePartialFile: Boolean,
    ) : DownloadTerminalResult

    data class Failed(
        val failure: DownloadFailure,
        val bytesDownloaded: Long,
        val totalBytes: Long?,
        val eTag: String?,
        val lastModified: String?,
    ) : DownloadTerminalResult
}
