package com.example.flowdownload

import android.content.Intent

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载动作可执行性快照，供宿主判断某个动作当前是否可点、是否需要额外授权以及可直接复用的跳转 Intent。
 */
data class FlowDownloadActionSupport(
    val actionType: FlowDownloadActionType,
    val canExecute: Boolean,
    val requiresUnknownSourcesPermission: Boolean = false,
    val unsupportedReason: FlowDownloadActionUnsupportedReason? = null,
    val intent: Intent? = null,
)
