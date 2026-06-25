package com.example.flowdownload

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ghn.lib.download.R
import androidx.work.ForegroundInfo
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadSnapshot
import com.example.flowdownload.download.model.DownloadStatus

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载通知渲染扩展点，允许宿主统一接管 WorkManager 前台通知、进程内活动通知和终态通知的渲染。
 */
interface FlowDownloadNotificationRenderer {
    /**
     * 创建或更新下载通知使用的通知渠道。
     */
    fun ensureChannel(
        context: Context,
        config: FlowDownloadNotificationConfig,
    ) = Unit

    /**
     * 为 `WORK_MANAGER` 模式创建前台服务通知。
     */
    fun createForegroundInfo(
        context: Context,
        config: FlowDownloadNotificationConfig,
        snapshot: DownloadSnapshot,
    ): ForegroundInfo

    /**
     * 为 `IN_PROCESS` 模式展示当前进行中的系统通知。
     *
     * 默认实现会复用 [createForegroundInfo] 生成的通知内容。
     */
    fun showActiveNotification(
        context: Context,
        config: FlowDownloadNotificationConfig,
        snapshot: DownloadSnapshot,
    ) {
        ensureChannel(context, config)
        val foregroundInfo = createForegroundInfo(context, config, snapshot)
        NotificationManagerCompat.from(context).notify(
            foregroundInfo.notificationId,
            foregroundInfo.notification,
        )
    }

    /**
     * 展示成功、失败、取消等终态通知。
     */
    fun showTerminalNotification(
        context: Context,
        config: FlowDownloadNotificationConfig,
        snapshot: DownloadSnapshot,
    )

    /**
     * 取消指定下载任务对应的通知。
     */
    fun cancelNotification(
        context: Context,
        config: FlowDownloadNotificationConfig,
        downloadId: DownloadId,
    )
}

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载通知渲染器注册中心，支持宿主运行时注入渲染器，也支持通过类名在恢复场景重新创建实例。
 */
object FlowDownloadNotificationRenderers {
    @Volatile
    private var installedRenderer: FlowDownloadNotificationRenderer? = null

    /**
     * 安装一个运行时通知渲染器。
     */
    fun install(renderer: FlowDownloadNotificationRenderer) {
        installedRenderer = renderer
    }

    /**
     * 清除当前运行时通知渲染器，后续会退回默认实现或 `rendererClassName`。
     */
    fun clear() {
        installedRenderer = null
    }

    /**
     * 按优先级解析当前有效的通知渲染器：
     * 1. 运行时安装的渲染器
     * 2. `rendererClassName`
     * 3. 默认实现
     */
    fun resolve(config: FlowDownloadNotificationConfig): FlowDownloadNotificationRenderer {
        installedRenderer?.let { return it }
        val className = config.rendererClassName ?: return DefaultFlowDownloadNotificationRenderer
        return instantiate(className)
    }

    private fun instantiate(className: String): FlowDownloadNotificationRenderer {
        val clazz = Class.forName(className)
        if (!FlowDownloadNotificationRenderer::class.java.isAssignableFrom(clazz)) {
            throw IllegalArgumentException(
                "Class $className does not implement FlowDownloadNotificationRenderer",
            )
        }

        val singleton = runCatching {
            clazz.getField("INSTANCE").get(null)
        }.getOrNull()
        if (singleton is FlowDownloadNotificationRenderer) {
            return singleton
        }

        val constructor = clazz.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance() as FlowDownloadNotificationRenderer
    }
}

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 默认下载通知渲染器，提供基础文案、进度展示、点击行为和通知动作按钮。
 */
object DefaultFlowDownloadNotificationRenderer : FlowDownloadNotificationRenderer {
    override fun ensureChannel(
        context: Context,
        config: FlowDownloadNotificationConfig,
    ) {
        if (!config.enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            config.channelId,
            config.channelName,
            NotificationManager.IMPORTANCE_LOW,
        )
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    override fun createForegroundInfo(
        context: Context,
        config: FlowDownloadNotificationConfig,
        snapshot: DownloadSnapshot,
    ): ForegroundInfo {
        ensureChannel(context, config)
        return ForegroundInfo(
            notificationId(snapshot.id),
            buildNotification(context, config, snapshot, ongoing = true),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }

    override fun showTerminalNotification(
        context: Context,
        config: FlowDownloadNotificationConfig,
        snapshot: DownloadSnapshot,
    ) {
        NotificationManagerCompat.from(context).notify(
            notificationId(snapshot.id),
            buildNotification(context, config, snapshot, ongoing = false),
        )
    }

    override fun cancelNotification(
        context: Context,
        config: FlowDownloadNotificationConfig,
        downloadId: DownloadId,
    ) {
        NotificationManagerCompat.from(context).cancel(notificationId(downloadId))
    }

    private fun buildNotification(
        context: Context,
        config: FlowDownloadNotificationConfig,
        snapshot: DownloadSnapshot,
        ongoing: Boolean,
    ): Notification {
        val progressKnown = snapshot.totalBytes != null && snapshot.totalBytes > 0L
        val builder = NotificationCompat.Builder(context, config.channelId)
            .setSmallIcon(iconFor(snapshot.status))
            .setContentTitle(titleFor(context, snapshot))
            .setContentText(messageFor(context, snapshot))
            .setOnlyAlertOnce(true)
            .setOngoing(ongoing)
            .setAutoCancel(!ongoing)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        FlowDownloadActions.createPrimaryFileActionPendingIntent(context, snapshot)
            ?.let(builder::setContentIntent)
        FlowDownloadActions.resolveNotificationActions(
            context = context,
            snapshot = snapshot,
            notificationConfig = config,
        ).forEach { action ->
            builder.addAction(action.iconResId, action.title, action.pendingIntent)
        }

        if (progressKnown) {
            builder.setProgress(100, snapshot.progressPercent ?: 0, false)
        } else if (
            snapshot.status is DownloadStatus.Starting ||
            snapshot.status is DownloadStatus.Running
        ) {
            builder.setProgress(0, 0, true)
        } else {
            builder.setProgress(0, 0, false)
        }

        return builder.build()
    }

    private fun iconFor(status: DownloadStatus): Int {
        return when (status) {
            is DownloadStatus.Success -> android.R.drawable.stat_sys_download_done
            is DownloadStatus.Failed -> android.R.drawable.stat_notify_error
            is DownloadStatus.Cancelled -> android.R.drawable.stat_notify_error
            else -> android.R.drawable.stat_sys_download
        }
    }

    private fun titleFor(
        context: Context,
        snapshot: DownloadSnapshot,
    ): String {
        val name = snapshot.displayName ?: snapshot.destination.substringAfterLast('/')
        return when (snapshot.status) {
            is DownloadStatus.Success ->
                context.getString(R.string.flowdownload_notification_title_success, name)

            is DownloadStatus.Failed ->
                context.getString(R.string.flowdownload_notification_title_failed, name)

            is DownloadStatus.Cancelled ->
                context.getString(R.string.flowdownload_notification_title_cancelled, name)

            is DownloadStatus.Paused ->
                context.getString(R.string.flowdownload_notification_title_paused, name)

            is DownloadStatus.RetryWaiting ->
                context.getString(R.string.flowdownload_notification_title_retry_waiting, name)

            else -> context.getString(R.string.flowdownload_notification_title_running, name)
        }
    }

    private fun messageFor(
        context: Context,
        snapshot: DownloadSnapshot,
    ): String {
        return when (val status = snapshot.status) {
            is DownloadStatus.Running ->
                snapshot.progressPercent?.let { progressPercent ->
                    context.getString(
                        R.string.flowdownload_notification_message_progress,
                        progressPercent,
                    )
                } ?: context.getString(R.string.flowdownload_notification_message_transferring)

            is DownloadStatus.Starting ->
                context.getString(R.string.flowdownload_notification_message_starting)

            is DownloadStatus.Queued ->
                context.getString(R.string.flowdownload_notification_message_queued)

            is DownloadStatus.RetryWaiting -> {
                if (snapshot.nextAttemptAtEpochMs != null) {
                    context.getString(
                        R.string.flowdownload_notification_message_retry_waiting_with_count,
                        snapshot.retryCount,
                        snapshot.maxRetries,
                    )
                } else {
                    context.getString(R.string.flowdownload_notification_message_retry_waiting)
                }
            }

            is DownloadStatus.Paused ->
                context.getString(R.string.flowdownload_notification_message_paused)

            is DownloadStatus.Success -> {
                if (snapshot.destination.startsWith(prefix = "content://", ignoreCase = true)) {
                    context.getString(
                        R.string.flowdownload_notification_message_success_content,
                        snapshot.displayName ?: snapshot.destination,
                    )
                } else {
                    context.getString(
                        R.string.flowdownload_notification_message_success_file,
                        snapshot.destination,
                    )
                }
            }

            is DownloadStatus.Cancelled ->
                context.getString(R.string.flowdownload_notification_message_cancelled)

            is DownloadStatus.Failed -> status.failure.message
        }
    }

    private fun notificationId(downloadId: DownloadId): Int {
        val raw = downloadId.value.hashCode()
        return if (raw == Int.MIN_VALUE) 0 else kotlin.math.abs(raw)
    }
}
