package com.example.flowdownload.download.worker

import android.content.Context
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
 *  描述: 进程内通知分发器，负责把下载快照转换为普通系统通知并复用宿主安装的通知渲染器。
 */
internal class AndroidInProcessDownloadNotificationDispatcher(
    context: Context,
    private val config: FlowDownloadNotificationConfig,
) : InProcessDownloadNotifier.Dispatcher {
    private val appContext = context.applicationContext
    private val renderer = FlowDownloadNotificationRenderers.resolve(config)

    override fun ensureChannel() {
        if (!config.enabled) {
            return
        }
        renderer.ensureChannel(appContext, config)
    }

    override fun showActive(snapshot: DownloadSnapshot) {
        if (!config.enabled) {
            return
        }
        renderer.showActiveNotification(appContext, config, snapshot)
    }

    override fun showTerminal(snapshot: DownloadSnapshot) {
        if (!config.enabled || !config.showTerminalNotifications) {
            return
        }
        renderer.showTerminalNotification(appContext, config, snapshot)
    }

    override fun cancel(downloadId: DownloadId) {
        renderer.cancelNotification(appContext, config, downloadId)
    }
}
