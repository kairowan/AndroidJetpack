package com.example.flowdownload.download.worker

import androidx.work.Data
import com.example.flowdownload.DownloadHttpStack
import com.example.flowdownload.DownloadRuntimeMode
import com.example.flowdownload.FlowDownloadConfig
import com.example.flowdownload.FlowDownloadNotificationConfig
import com.example.flowdownload.download.model.DownloadId

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: WorkManager 输入参数编解码器，负责在唯一任务请求中传递下载 ID 和运行配置。
 */
object DownloadWorkDataCodec {
    private const val KEY_DOWNLOAD_ID = "download_id"
    private const val KEY_DATABASE_NAME = "database_name"
    private const val KEY_MAX_CONCURRENT_DOWNLOADS = "max_concurrent_downloads"
    private const val KEY_CONNECT_TIMEOUT = "connect_timeout"
    private const val KEY_READ_TIMEOUT = "read_timeout"
    private const val KEY_PROGRESS_THROTTLE = "progress_throttle"
    private const val KEY_RETRY_BACKOFF = "retry_backoff"
    private const val KEY_AUTO_RECOVER_QUEUED = "auto_recover_queued"
    private const val KEY_AUTO_PAUSE_INTERRUPTED = "auto_pause_interrupted"
    private const val KEY_HTTP_STACK = "http_stack"
    private const val KEY_HTTP_COMPONENT_FACTORY_CLASS_NAME = "http_component_factory_class_name"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_NOTIFICATION_CHANNEL_ID = "notification_channel_id"
    private const val KEY_NOTIFICATION_CHANNEL_NAME = "notification_channel_name"
    private const val KEY_SHOW_TERMINAL_NOTIFICATIONS = "show_terminal_notifications"
    private const val KEY_SHOW_CONTROL_ACTIONS = "show_control_actions"
    private const val KEY_SHOW_TERMINAL_ACTIONS = "show_terminal_actions"
    private const val KEY_INCLUDE_SHARE_ACTION_IN_TERMINAL_ACTIONS =
        "include_share_action_in_terminal_actions"
    private const val KEY_NOTIFICATION_RENDERER_CLASS_NAME = "notification_renderer_class_name"

    fun encode(
        downloadId: DownloadId,
        config: FlowDownloadConfig,
    ): Data = Data.Builder()
        .putString(KEY_DOWNLOAD_ID, downloadId.value)
        .putString(KEY_DATABASE_NAME, config.databaseName)
        .putInt(KEY_MAX_CONCURRENT_DOWNLOADS, config.maxConcurrentDownloads)
        .putInt(KEY_CONNECT_TIMEOUT, config.connectTimeoutMillis)
        .putInt(KEY_READ_TIMEOUT, config.readTimeoutMillis)
        .putLong(KEY_PROGRESS_THROTTLE, config.progressThrottleMillis)
        .putLong(KEY_RETRY_BACKOFF, config.retryBackoffMillis)
        .putBoolean(KEY_AUTO_RECOVER_QUEUED, config.autoRecoverQueuedDownloads)
        .putBoolean(KEY_AUTO_PAUSE_INTERRUPTED, config.autoPauseInterruptedDownloads)
        .putString(KEY_HTTP_STACK, config.httpStack.name)
        .putString(KEY_HTTP_COMPONENT_FACTORY_CLASS_NAME, config.httpComponentFactoryClassName)
        .putBoolean(KEY_NOTIFICATIONS_ENABLED, config.notification.enabled)
        .putString(KEY_NOTIFICATION_CHANNEL_ID, config.notification.channelId)
        .putString(KEY_NOTIFICATION_CHANNEL_NAME, config.notification.channelName)
        .putBoolean(
            KEY_SHOW_TERMINAL_NOTIFICATIONS,
            config.notification.showTerminalNotifications,
        )
        .putBoolean(KEY_SHOW_CONTROL_ACTIONS, config.notification.showControlActions)
        .putBoolean(KEY_SHOW_TERMINAL_ACTIONS, config.notification.showTerminalActions)
        .putBoolean(
            KEY_INCLUDE_SHARE_ACTION_IN_TERMINAL_ACTIONS,
            config.notification.includeShareActionInTerminalActions,
        )
        .putString(
            KEY_NOTIFICATION_RENDERER_CLASS_NAME,
            config.notification.rendererClassName,
        )
        .build()

    fun decodeDownloadId(data: Data): DownloadId {
        val value = requireNotNull(data.getString(KEY_DOWNLOAD_ID)) {
            "Missing download id for FlowDownload worker"
        }
        return DownloadId(value)
    }

    fun decodeConfig(data: Data): FlowDownloadConfig {
        val notification = FlowDownloadNotificationConfig(
            enabled = data.getBoolean(KEY_NOTIFICATIONS_ENABLED, true),
            channelId = data.getString(KEY_NOTIFICATION_CHANNEL_ID) ?: "flowdownload.active",
            channelName = data.getString(KEY_NOTIFICATION_CHANNEL_NAME) ?: "Flow Download",
            showTerminalNotifications = data.getBoolean(KEY_SHOW_TERMINAL_NOTIFICATIONS, true),
            showControlActions = data.getBoolean(KEY_SHOW_CONTROL_ACTIONS, true),
            showTerminalActions = data.getBoolean(KEY_SHOW_TERMINAL_ACTIONS, true),
            includeShareActionInTerminalActions = data.getBoolean(
                KEY_INCLUDE_SHARE_ACTION_IN_TERMINAL_ACTIONS,
                false,
            ),
            rendererClassName = data.getString(KEY_NOTIFICATION_RENDERER_CLASS_NAME),
        )
        return FlowDownloadConfig(
            databaseName = data.getString(KEY_DATABASE_NAME) ?: "flowdownload.db",
            maxConcurrentDownloads = data.getInt(KEY_MAX_CONCURRENT_DOWNLOADS, 2),
            connectTimeoutMillis = data.getInt(KEY_CONNECT_TIMEOUT, 15_000),
            readTimeoutMillis = data.getInt(KEY_READ_TIMEOUT, 15_000),
            progressThrottleMillis = data.getLong(KEY_PROGRESS_THROTTLE, 200L),
            retryBackoffMillis = data.getLong(KEY_RETRY_BACKOFF, 30_000L),
            autoRecoverQueuedDownloads = data.getBoolean(KEY_AUTO_RECOVER_QUEUED, true),
            autoPauseInterruptedDownloads = data.getBoolean(KEY_AUTO_PAUSE_INTERRUPTED, true),
            runtimeMode = DownloadRuntimeMode.WORK_MANAGER,
            httpStack = data.getString(KEY_HTTP_STACK)?.let(DownloadHttpStack::valueOf)
                ?: DownloadHttpStack.OKHTTP,
            httpComponentFactoryClassName = data.getString(KEY_HTTP_COMPONENT_FACTORY_CLASS_NAME),
            notification = notification,
        )
    }

    fun uniqueWorkName(downloadId: DownloadId): String = "flowdownload.${downloadId.value}"
}
