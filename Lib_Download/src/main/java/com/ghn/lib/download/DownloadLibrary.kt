package com.ghn.lib.download

import android.content.Context
import com.example.flowdownload.FlowDownload
import com.example.flowdownload.FlowDownloadConfig
import com.example.flowdownload.FlowDownloadNotificationConfig

/**
 * @author 浩楠
 *
 * @date 2026/6/24
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 下载库对外配置与入口定义，负责封装宿主侧初始化能力。
 */

/**
 * 下载库宿主配置模型。
 */
data class DownloadLibraryConfig(
    val maxConcurrentDownloads: Int = 3,
    val retryBackoffMillis: Long = 10_000L,
    val notificationsEnabled: Boolean = false,
    val showTerminalNotifications: Boolean = false,
    val showControlActions: Boolean = false,
    val showTerminalActions: Boolean = false,
)

/**
 * 下载库对外入口。
 */
object DownloadLibrary {
    @Volatile
    private var initialized = false

    fun configure(config: DownloadLibraryConfig = DownloadLibraryConfig()) {
        synchronized(this) {
            // 这里只记录配置，不主动创建底层 client，便于把真正的初始化后移。
            FlowDownload.configure(config = config.toFlowDownloadConfig())
        }
    }

    fun warmUp(context: Context) {
        if (initialized) {
            return
        }
        synchronized(this) {
            if (initialized) {
                return
            }
            // 通过惰性 get 触发底层实例创建，适合放在首帧后的后台预热阶段。
            FlowDownload.get(context.applicationContext)
            initialized = true
        }
    }

    fun initialize(
        context: Context,
        config: DownloadLibraryConfig = DownloadLibraryConfig(),
    ) {
        configure(config)
        warmUp(context)
    }

    fun reset() {
        synchronized(this) {
            FlowDownload.reset()
            initialized = false
        }
    }
}

private fun DownloadLibraryConfig.toFlowDownloadConfig(): FlowDownloadConfig {
    return FlowDownloadConfig(
        maxConcurrentDownloads = maxConcurrentDownloads,
        retryBackoffMillis = retryBackoffMillis,
        notification = FlowDownloadNotificationConfig(
            enabled = notificationsEnabled,
            showTerminalNotifications = showTerminalNotifications,
            showControlActions = showControlActions,
            showTerminalActions = showTerminalActions
        )
    )
}
