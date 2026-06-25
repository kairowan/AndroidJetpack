package com.example.flowdownload

import android.content.Intent
import com.example.flowdownload.download.model.DownloadId

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载动作统一执行结果，向宿主返回命令已派发、页面已跳转或当前动作不可执行等稳定结果。
 */
sealed interface FlowDownloadActionExecutionResult {
    val actionType: FlowDownloadActionType

    data class CommandDispatched(
        override val actionType: FlowDownloadActionType,
        val downloadId: DownloadId,
    ) : FlowDownloadActionExecutionResult

    data class ActivityStarted(
        override val actionType: FlowDownloadActionType,
        val intent: Intent,
    ) : FlowDownloadActionExecutionResult

    data class Unsupported(
        override val actionType: FlowDownloadActionType,
        val reason: FlowDownloadActionUnsupportedReason,
        val message: String? = null,
    ) : FlowDownloadActionExecutionResult
}

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载动作不可执行原因枚举，供宿主区分状态不匹配、缺少执行器、文件资源不可用或没有可处理页面等场景。
 */
enum class FlowDownloadActionUnsupportedReason {
    ACTION_NOT_AVAILABLE,
    COMMAND_EXECUTOR_UNAVAILABLE,
    FILE_RESOURCE_UNAVAILABLE,
    UNKNOWN_SOURCES_PERMISSION_REQUIRED,
    NO_ACTIVITY_HANDLER,
    ACTIVITY_START_FAILED,
}
