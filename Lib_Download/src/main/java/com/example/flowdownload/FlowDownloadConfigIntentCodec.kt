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
 *  描述: 下载配置 Intent 编解码器，用于在通知动作或其他进程恢复入口里携带并重建运行配置。
 */
internal object FlowDownloadConfigIntentCodec {
    private const val KEY_DATABASE_NAME = "flowdownload.database_name"
    private const val KEY_MAX_CONCURRENT_DOWNLOADS = "flowdownload.max_concurrent_downloads"
    private const val KEY_CONNECT_TIMEOUT = "flowdownload.connect_timeout"
    private const val KEY_READ_TIMEOUT = "flowdownload.read_timeout"
    private const val KEY_PROGRESS_THROTTLE = "flowdownload.progress_throttle"
    private const val KEY_RETRY_BACKOFF = "flowdownload.retry_backoff"
    private const val KEY_AUTO_RECOVER_QUEUED = "flowdownload.auto_recover_queued"
    private const val KEY_AUTO_PAUSE_INTERRUPTED = "flowdownload.auto_pause_interrupted"
    private const val KEY_RUNTIME_MODE = "flowdownload.runtime_mode"
    private const val KEY_HTTP_STACK = "flowdownload.http_stack"
    private const val KEY_HTTP_COMPONENT_FACTORY_CLASS_NAME =
        "flowdownload.http_component_factory_class_name"
    private const val KEY_NOTIFICATION_ENABLED = "flowdownload.notification_enabled"
    private const val KEY_NOTIFICATION_CHANNEL_ID = "flowdownload.notification_channel_id"
    private const val KEY_NOTIFICATION_CHANNEL_NAME = "flowdownload.notification_channel_name"
    private const val KEY_SHOW_TERMINAL_NOTIFICATIONS =
        "flowdownload.show_terminal_notifications"
    private const val KEY_SHOW_CONTROL_ACTIONS = "flowdownload.show_control_actions"
    private const val KEY_SHOW_TERMINAL_ACTIONS = "flowdownload.show_terminal_actions"
    private const val KEY_INCLUDE_SHARE_TERMINAL_ACTION =
        "flowdownload.include_share_terminal_action"
    private const val KEY_NOTIFICATION_RENDERER_CLASS_NAME =
        "flowdownload.notification_renderer_class_name"

    fun encode(
        intent: Intent,
        config: FlowDownloadConfig,
    ): Intent {
        return intent
            .putExtra(KEY_DATABASE_NAME, config.databaseName)
            .putExtra(KEY_MAX_CONCURRENT_DOWNLOADS, config.maxConcurrentDownloads)
            .putExtra(KEY_CONNECT_TIMEOUT, config.connectTimeoutMillis)
            .putExtra(KEY_READ_TIMEOUT, config.readTimeoutMillis)
            .putExtra(KEY_PROGRESS_THROTTLE, config.progressThrottleMillis)
            .putExtra(KEY_RETRY_BACKOFF, config.retryBackoffMillis)
            .putExtra(KEY_AUTO_RECOVER_QUEUED, config.autoRecoverQueuedDownloads)
            .putExtra(KEY_AUTO_PAUSE_INTERRUPTED, config.autoPauseInterruptedDownloads)
            .putExtra(KEY_RUNTIME_MODE, config.runtimeMode.name)
            .putExtra(KEY_HTTP_STACK, config.httpStack.name)
            .putExtra(
                KEY_HTTP_COMPONENT_FACTORY_CLASS_NAME,
                config.httpComponentFactoryClassName,
            )
            .putExtra(KEY_NOTIFICATION_ENABLED, config.notification.enabled)
            .putExtra(KEY_NOTIFICATION_CHANNEL_ID, config.notification.channelId)
            .putExtra(KEY_NOTIFICATION_CHANNEL_NAME, config.notification.channelName)
            .putExtra(
                KEY_SHOW_TERMINAL_NOTIFICATIONS,
                config.notification.showTerminalNotifications,
            )
            .putExtra(KEY_SHOW_CONTROL_ACTIONS, config.notification.showControlActions)
            .putExtra(KEY_SHOW_TERMINAL_ACTIONS, config.notification.showTerminalActions)
            .putExtra(
                KEY_INCLUDE_SHARE_TERMINAL_ACTION,
                config.notification.includeShareActionInTerminalActions,
            )
            .putExtra(
                KEY_NOTIFICATION_RENDERER_CLASS_NAME,
                config.notification.rendererClassName,
            )
    }

    fun decode(intent: Intent): FlowDownloadConfig {
        val notification = FlowDownloadNotificationConfig(
            enabled = intent.getBooleanExtra(KEY_NOTIFICATION_ENABLED, true),
            channelId = intent.getStringExtra(KEY_NOTIFICATION_CHANNEL_ID) ?: "flowdownload.active",
            channelName = intent.getStringExtra(KEY_NOTIFICATION_CHANNEL_NAME)
                ?: "Flow Download",
            showTerminalNotifications = intent.getBooleanExtra(
                KEY_SHOW_TERMINAL_NOTIFICATIONS,
                true,
            ),
            showControlActions = intent.getBooleanExtra(KEY_SHOW_CONTROL_ACTIONS, true),
            showTerminalActions = intent.getBooleanExtra(KEY_SHOW_TERMINAL_ACTIONS, true),
            includeShareActionInTerminalActions = intent.getBooleanExtra(
                KEY_INCLUDE_SHARE_TERMINAL_ACTION,
                false,
            ),
            rendererClassName = intent.getStringExtra(KEY_NOTIFICATION_RENDERER_CLASS_NAME),
        )

        return FlowDownloadConfig(
            databaseName = intent.getStringExtra(KEY_DATABASE_NAME) ?: "flowdownload.db",
            maxConcurrentDownloads = intent.getIntExtra(KEY_MAX_CONCURRENT_DOWNLOADS, 2),
            connectTimeoutMillis = intent.getIntExtra(KEY_CONNECT_TIMEOUT, 15_000),
            readTimeoutMillis = intent.getIntExtra(KEY_READ_TIMEOUT, 15_000),
            progressThrottleMillis = intent.getLongExtra(KEY_PROGRESS_THROTTLE, 200L),
            retryBackoffMillis = intent.getLongExtra(KEY_RETRY_BACKOFF, 30_000L),
            autoRecoverQueuedDownloads = intent.getBooleanExtra(KEY_AUTO_RECOVER_QUEUED, true),
            autoPauseInterruptedDownloads = intent.getBooleanExtra(
                KEY_AUTO_PAUSE_INTERRUPTED,
                true,
            ),
            runtimeMode = intent.getStringExtra(KEY_RUNTIME_MODE)
                ?.let(DownloadRuntimeMode::valueOf)
                ?: DownloadRuntimeMode.IN_PROCESS,
            httpStack = intent.getStringExtra(KEY_HTTP_STACK)
                ?.let(DownloadHttpStack::valueOf)
                ?: DownloadHttpStack.OKHTTP,
            httpComponentFactoryClassName = intent.getStringExtra(
                KEY_HTTP_COMPONENT_FACTORY_CLASS_NAME,
            ),
            notification = notification,
        )
    }
}
