package com.example.flowdownload.download.data

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 持久化停止请求模型，用于在进程内调度与 WorkManager 运行时之间传递暂停和取消意图。
 */
data class PendingStopRequest(
    val reason: PendingStopReason,
    val deletePartialFile: Boolean,
)

/**
 * =停止请求原因枚举，内部用于区分用户发起的暂停与取消动作。
 */
enum class PendingStopReason {
    PAUSE,
    CANCEL,
}
