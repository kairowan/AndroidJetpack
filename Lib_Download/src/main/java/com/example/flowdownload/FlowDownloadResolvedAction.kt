package com.example.flowdownload

import android.content.Intent
import android.net.Uri

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 已解析完成的宿主动作，包含可直接用于页面或通知点击的 Intent、Uri 和 MimeType。
 */
data class FlowDownloadResolvedAction(
    val type: FlowDownloadActionType,
    val uri: Uri,
    val mimeType: String?,
    val intent: Intent,
)
