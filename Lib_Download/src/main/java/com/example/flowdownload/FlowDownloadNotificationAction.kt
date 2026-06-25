package com.example.flowdownload

import android.app.PendingIntent

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 通知动作描述模型，封装通知按钮类型、文案、图标以及可直接挂载的 PendingIntent。
 */
data class FlowDownloadNotificationAction(
    val type: FlowDownloadActionType,
    val title: CharSequence,
    val iconResId: Int,
    val pendingIntent: PendingIntent,
)
