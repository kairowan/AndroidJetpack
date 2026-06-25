package com.example.flowdownload

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载宿主动作类型，统一描述控制类动作以及成功后可触发的文件类动作。
 */
enum class FlowDownloadActionType {
    PAUSE,
    RESUME,
    CANCEL,
    RETRY,
    OPEN,
    INSTALL,
    SHARE,
    ;

    val isFileAction: Boolean
        get() = this == OPEN || this == INSTALL || this == SHARE

    val isCommandAction: Boolean
        get() = !isFileAction
}
