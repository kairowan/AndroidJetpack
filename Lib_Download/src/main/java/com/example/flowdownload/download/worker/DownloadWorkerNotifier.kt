package com.example.flowdownload.download.worker

import android.content.Context
import androidx.work.ForegroundInfo
import com.example.flowdownload.FlowDownloadNotificationConfig
import com.example.flowdownload.FlowDownloadNotificationRenderers
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadSnapshot

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载 Worker 通知门面，负责将宿主可替换的通知渲染器接入后台下载生命周期。
 */
class DownloadWorkerNotifier(
    private val context: Context,
    private val config: FlowDownloadNotificationConfig,
) {
    private val renderer = FlowDownloadNotificationRenderers.resolve(config)

    fun ensureChannel() {
        renderer.ensureChannel(context, config)
    }

    fun createForegroundInfo(snapshot: DownloadSnapshot): ForegroundInfo {
        return renderer.createForegroundInfo(context, config, snapshot)
    }

    fun showTerminal(snapshot: DownloadSnapshot) {
        if (!config.enabled || !config.showTerminalNotifications) return
        renderer.showTerminalNotification(context, config, snapshot)
    }

    fun cancel(downloadId: DownloadId) {
        renderer.cancelNotification(context, config, downloadId)
    }
}
