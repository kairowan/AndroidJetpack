package com.example.flowdownload

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.webkit.MimeTypeMap
import com.ghn.lib.download.R
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadSnapshot
import com.example.flowdownload.download.model.DownloadStatus
import kotlin.math.abs

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载宿主动作辅助入口，统一提供按钮渲染所需动作枚举以及打开、安装、分享相关 Intent/PendingIntent 构建能力。
 */
object FlowDownloadActions {
    private const val MimeTypeApk = "application/vnd.android.package-archive"
    private const val MimeTypeFallback = "*/*"
    private const val FileLabelFallback = "download"
    private const val PendingIntentFlags =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    /**
     * 解析一个任务当前可展示的全部动作。
     *
     * 结果会按“控制动作在前、文件动作在后”的顺序返回。
     */
    fun availableActionTypes(
        snapshot: DownloadSnapshot,
        includeShare: Boolean = true,
        includeOpenForInstallable: Boolean = false,
    ): List<FlowDownloadActionType> {
        return buildList {
            addAll(availableControlActions(snapshot))
            addAll(
                availableFileActions(
                    snapshot = snapshot,
                    includeShare = includeShare,
                    includeOpenForInstallable = includeOpenForInstallable,
                ),
            )
        }
    }

    /**
     * 仅解析暂停、继续、取消、重试等控制动作。
     */
    fun availableControlActions(snapshot: DownloadSnapshot): List<FlowDownloadActionType> {
        return buildList {
            if (snapshot.canPause) {
                add(FlowDownloadActionType.PAUSE)
            }
            if (snapshot.canResume) {
                add(FlowDownloadActionType.RESUME)
            }
            if (snapshot.canCancel) {
                add(FlowDownloadActionType.CANCEL)
            }
            if (snapshot.canRetry) {
                add(FlowDownloadActionType.RETRY)
            }
        }
    }

    /**
     * 判断某个动作在当前快照下是否应当被展示。
     */
    fun isActionAvailable(
        snapshot: DownloadSnapshot,
        actionType: FlowDownloadActionType,
        includeShare: Boolean = true,
        includeOpenForInstallable: Boolean = false,
    ): Boolean {
        return availableActionTypes(
            snapshot = snapshot,
            includeShare = includeShare,
            includeOpenForInstallable = includeOpenForInstallable,
        ).contains(actionType)
    }

    /**
     * 仅解析成功后的文件动作，如打开、安装、分享。
     */
    fun availableFileActions(
        snapshot: DownloadSnapshot,
        includeShare: Boolean = true,
        includeOpenForInstallable: Boolean = false,
    ): List<FlowDownloadActionType> {
        if (snapshot.status !is DownloadStatus.Success) {
            return emptyList()
        }

        return buildList {
            if (isInstallable(snapshot)) {
                add(FlowDownloadActionType.INSTALL)
                if (includeOpenForInstallable) {
                    add(FlowDownloadActionType.OPEN)
                }
            } else {
                add(FlowDownloadActionType.OPEN)
            }

            if (includeShare) {
                add(FlowDownloadActionType.SHARE)
            }
        }
    }

    /**
     * 根据通知配置解析当前通知应展示的动作集合。
     */
    fun notificationActionTypes(
        config: FlowDownloadNotificationConfig,
        snapshot: DownloadSnapshot,
        includeOpenForInstallable: Boolean = false,
    ): List<FlowDownloadActionType> {
        if (snapshot.status is DownloadStatus.Success) {
            if (!config.showTerminalActions) {
                return emptyList()
            }

            return availableFileActions(
                snapshot = snapshot,
                includeShare = config.includeShareActionInTerminalActions,
                includeOpenForInstallable = includeOpenForInstallable,
            )
        }

        if (!config.showControlActions) {
            return emptyList()
        }

        return availableControlActions(snapshot)
    }

    /**
     * 获取一个任务的默认主文件动作。
     *
     * 普通文件通常为 `OPEN`，APK 通常为 `INSTALL`。
     */
    fun primaryFileActionType(
        snapshot: DownloadSnapshot,
        includeOpenForInstallable: Boolean = false,
    ): FlowDownloadActionType? {
        return availableFileActions(
            snapshot = snapshot,
            includeShare = false,
            includeOpenForInstallable = includeOpenForInstallable,
        ).firstOrNull()
    }

    /**
     * 解析下载结果的只读 Uri。
     */
    fun resolveReadUri(
        context: Context,
        snapshot: DownloadSnapshot,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
    ): Uri? = runCatching { uriResolver.resolve(context, snapshot) }.getOrNull()

    /**
     * 解析下载结果的 MIME Type。
     */
    fun resolveMimeType(
        context: Context,
        snapshot: DownloadSnapshot,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
    ): String? {
        val uri = resolveReadUri(context, snapshot, uriResolver)
        return resolveMimeType(context, snapshot, uri)
    }

    /**
     * 解析成功态文件动作，并直接返回宿主可复用的完整动作对象。
     */
    fun resolveTerminalActions(
        context: Context,
        snapshot: DownloadSnapshot,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        includeShare: Boolean = true,
        includeOpenForInstallable: Boolean = false,
    ): List<FlowDownloadResolvedAction> {
        val uri = resolveReadUri(context, snapshot, uriResolver) ?: return emptyList()
        val mimeType = resolveMimeType(context, snapshot, uri)
        return availableFileActions(
            snapshot = snapshot,
            includeShare = includeShare,
            includeOpenForInstallable = includeOpenForInstallable,
        ).mapNotNull { actionType ->
            createFileActionIntent(
                context = context,
                snapshot = snapshot,
                actionType = actionType,
                resolvedUri = uri,
                resolvedMimeType = mimeType,
            )?.let { intent ->
                FlowDownloadResolvedAction(
                    type = actionType,
                    uri = uri,
                    mimeType = effectiveMimeType(actionType, mimeType),
                    intent = intent,
                )
            }
        }
    }

    /**
     * 构建默认主文件动作对应的 `Intent`。
     */
    fun createPrimaryFileActionIntent(
        context: Context,
        snapshot: DownloadSnapshot,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        includeOpenForInstallable: Boolean = false,
    ): Intent? {
        val actionType = primaryFileActionType(
            snapshot = snapshot,
            includeOpenForInstallable = includeOpenForInstallable,
        ) ?: return null

        return createFileActionIntent(
            context = context,
            snapshot = snapshot,
            actionType = actionType,
            uriResolver = uriResolver,
        )
    }

    /**
     * 为指定文件动作构建 `Intent`。
     */
    fun createFileActionIntent(
        context: Context,
        snapshot: DownloadSnapshot,
        actionType: FlowDownloadActionType,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
    ): Intent? {
        if (!actionType.isFileAction) {
            throw IllegalArgumentException("Action $actionType is not a file action")
        }

        val uri = resolveReadUri(context, snapshot, uriResolver) ?: return null
        val mimeType = resolveMimeType(context, snapshot, uri)
        return createFileActionIntent(
            context = context,
            snapshot = snapshot,
            actionType = actionType,
            resolvedUri = uri,
            resolvedMimeType = mimeType,
        )
    }

    /**
     * 构建默认主文件动作对应的 `PendingIntent`。
     */
    fun createPrimaryFileActionPendingIntent(
        context: Context,
        snapshot: DownloadSnapshot,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        includeOpenForInstallable: Boolean = false,
        flags: Int = PendingIntentFlags,
    ): PendingIntent? {
        val actionType = primaryFileActionType(
            snapshot = snapshot,
            includeOpenForInstallable = includeOpenForInstallable,
        ) ?: return null

        return createFileActionPendingIntent(
            context = context,
            snapshot = snapshot,
            actionType = actionType,
            uriResolver = uriResolver,
            requestCode = requestCode(snapshot.id, actionType),
            flags = flags,
        )
    }

    /**
     * 为指定文件动作构建 `PendingIntent`。
     */
    fun createFileActionPendingIntent(
        context: Context,
        snapshot: DownloadSnapshot,
        actionType: FlowDownloadActionType,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        requestCode: Int = requestCode(snapshot.id, actionType),
        flags: Int = PendingIntentFlags,
    ): PendingIntent? {
        val intent = createFileActionIntent(
            context = context,
            snapshot = snapshot,
            actionType = actionType,
            uriResolver = uriResolver,
        ) ?: return null

        return PendingIntent.getActivity(context, requestCode, intent, flags)
    }

    /**
     * 为命令动作构建广播型 `PendingIntent`，常用于通知按钮。
     */
    fun createCommandActionPendingIntent(
        context: Context,
        downloadId: DownloadId,
        actionType: FlowDownloadActionType,
        config: FlowDownloadConfig? = FlowDownload.peekConfig(),
        requestCode: Int = requestCode(downloadId, actionType),
        flags: Int = PendingIntentFlags,
    ): PendingIntent? {
        if (!actionType.isCommandAction) {
            throw IllegalArgumentException("Action $actionType is not a command action")
        }
        val resolvedConfig = config ?: return null

        val intent = FlowDownloadCommandReceiver.createIntent(
            context = context,
            config = resolvedConfig,
            downloadId = downloadId,
            actionType = actionType,
        )
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    /**
     * 解析通知按钮模型，供宿主或默认通知渲染器直接挂到通知上。
     */
    fun resolveNotificationActions(
        context: Context,
        snapshot: DownloadSnapshot,
        notificationConfig: FlowDownloadNotificationConfig,
        runtimeConfig: FlowDownloadConfig? = FlowDownload.peekConfig(),
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        includeOpenForInstallable: Boolean = false,
    ): List<FlowDownloadNotificationAction> {
        return notificationActionTypes(
            config = notificationConfig,
            snapshot = snapshot,
            includeOpenForInstallable = includeOpenForInstallable,
        ).mapNotNull { actionType ->
            val pendingIntent = when {
                actionType.isCommandAction -> createCommandActionPendingIntent(
                    context = context,
                    downloadId = snapshot.id,
                    actionType = actionType,
                    config = runtimeConfig,
                )

                actionType.isFileAction -> createFileActionPendingIntent(
                    context = context,
                    snapshot = snapshot,
                    actionType = actionType,
                    uriResolver = uriResolver,
                )

                else -> null
            } ?: return@mapNotNull null

            FlowDownloadNotificationAction(
                type = actionType,
                title = defaultActionTitle(context, actionType),
                iconResId = defaultActionIconRes(actionType),
                pendingIntent = pendingIntent,
            )
        }
    }

    /**
     * 便捷重载：直接使用完整运行配置解析通知动作。
     */
    fun resolveNotificationActions(
        context: Context,
        config: FlowDownloadConfig,
        snapshot: DownloadSnapshot,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        includeOpenForInstallable: Boolean = false,
    ): List<FlowDownloadNotificationAction> {
        return resolveNotificationActions(
            context = context,
            snapshot = snapshot,
            notificationConfig = config.notification,
            runtimeConfig = config,
            uriResolver = uriResolver,
            includeOpenForInstallable = includeOpenForInstallable,
        )
    }

    /**
     * 解析某个动作当前是否可真正执行，并返回失败原因或可复用的 `Intent`。
     */
    fun resolveActionSupport(
        context: Context,
        snapshot: DownloadSnapshot,
        actionType: FlowDownloadActionType,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        wrapShareInChooser: Boolean = true,
    ): FlowDownloadActionSupport {
        if (!isActionAvailable(snapshot, actionType)) {
            return FlowDownloadActionSupport(
                actionType = actionType,
                canExecute = false,
                unsupportedReason = FlowDownloadActionUnsupportedReason.ACTION_NOT_AVAILABLE,
            )
        }

        if (actionType.isCommandAction) {
            return FlowDownloadActionSupport(
                actionType = actionType,
                canExecute = true,
            )
        }

        val fileIntent = createFileActionIntent(
            context = context,
            snapshot = snapshot,
            actionType = actionType,
            uriResolver = uriResolver,
        ) ?: return FlowDownloadActionSupport(
            actionType = actionType,
            canExecute = false,
            unsupportedReason = FlowDownloadActionUnsupportedReason.FILE_RESOURCE_UNAVAILABLE,
        )

        if (actionType == FlowDownloadActionType.INSTALL &&
            requiresUnknownSourcesPermission(context, actionType)
        ) {
            return FlowDownloadActionSupport(
                actionType = actionType,
                canExecute = false,
                requiresUnknownSourcesPermission = true,
                unsupportedReason = FlowDownloadActionUnsupportedReason.UNKNOWN_SOURCES_PERMISSION_REQUIRED,
                intent = createManageUnknownSourcesIntent(context),
            )
        }

        val launchIntent = prepareLaunchIntent(
            context = context,
            actionType = actionType,
            intent = fileIntent,
            wrapShareInChooser = wrapShareInChooser,
        )

        if (launchIntent.resolveActivity(context.packageManager) == null) {
            return FlowDownloadActionSupport(
                actionType = actionType,
                canExecute = false,
                unsupportedReason = FlowDownloadActionUnsupportedReason.NO_ACTIVITY_HANDLER,
            )
        }

        return FlowDownloadActionSupport(
            actionType = actionType,
            canExecute = true,
            intent = launchIntent,
        )
    }

    /**
     * 获取当前环境下既可展示又可执行的动作集合。
     */
    fun resolveExecutableActionTypes(
        context: Context,
        snapshot: DownloadSnapshot,
        includeShare: Boolean = true,
        includeOpenForInstallable: Boolean = false,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        wrapShareInChooser: Boolean = true,
    ): List<FlowDownloadActionType> {
        return availableActionTypes(
            snapshot = snapshot,
            includeShare = includeShare,
            includeOpenForInstallable = includeOpenForInstallable,
        ).filter { actionType ->
            resolveActionSupport(
                context = context,
                snapshot = snapshot,
                actionType = actionType,
                uriResolver = uriResolver,
                wrapShareInChooser = wrapShareInChooser,
            ).canExecute
        }
    }

    /**
     * 判断当前应用是否具备未知来源安装权限。
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return context.packageManager.canRequestPackageInstalls()
    }

    /**
     * 判断安装动作当前是否还需要用户授予未知来源安装权限。
     */
    fun requiresUnknownSourcesPermission(
        context: Context,
        actionType: FlowDownloadActionType,
    ): Boolean {
        return actionType == FlowDownloadActionType.INSTALL && !canRequestPackageInstalls(context)
    }

    /**
     * 构建跳转到“允许安装未知来源应用”页面的 `Intent`。
     */
    fun createManageUnknownSourcesIntent(context: Context): Intent {
        return Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData(Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * 获取动作的默认英文标题。
     */
    fun defaultActionTitle(actionType: FlowDownloadActionType): String {
        return when (actionType) {
            FlowDownloadActionType.PAUSE -> "Pause"
            FlowDownloadActionType.RESUME -> "Resume"
            FlowDownloadActionType.CANCEL -> "Cancel"
            FlowDownloadActionType.RETRY -> "Retry"
            FlowDownloadActionType.OPEN -> "Open"
            FlowDownloadActionType.INSTALL -> "Install"
            FlowDownloadActionType.SHARE -> "Share"
        }
    }

    /**
     * 获取动作的资源化标题文案。
     */
    fun defaultActionTitle(
        context: Context,
        actionType: FlowDownloadActionType,
    ): String {
        return when (actionType) {
            FlowDownloadActionType.PAUSE -> context.getString(R.string.flowdownload_action_pause)
            FlowDownloadActionType.RESUME -> context.getString(R.string.flowdownload_action_resume)
            FlowDownloadActionType.CANCEL -> context.getString(R.string.flowdownload_action_cancel)
            FlowDownloadActionType.RETRY -> context.getString(R.string.flowdownload_action_retry)
            FlowDownloadActionType.OPEN -> context.getString(R.string.flowdownload_action_open)
            FlowDownloadActionType.INSTALL -> context.getString(R.string.flowdownload_action_install)
            FlowDownloadActionType.SHARE -> context.getString(R.string.flowdownload_action_share)
        }
    }

    /**
     * 获取动作默认图标资源。
     */
    fun defaultActionIconRes(actionType: FlowDownloadActionType): Int {
        return when (actionType) {
            FlowDownloadActionType.PAUSE -> android.R.drawable.ic_media_pause
            FlowDownloadActionType.RESUME -> android.R.drawable.ic_media_play
            FlowDownloadActionType.CANCEL -> android.R.drawable.ic_menu_close_clear_cancel
            FlowDownloadActionType.RETRY -> android.R.drawable.stat_notify_sync
            FlowDownloadActionType.OPEN -> android.R.drawable.ic_menu_view
            FlowDownloadActionType.INSTALL -> android.R.drawable.stat_sys_download_done
            FlowDownloadActionType.SHARE -> android.R.drawable.ic_menu_share
        }
    }

    /**
     * 使用当前全局客户端执行一个动作。
     */
    suspend fun executeAction(
        context: Context,
        snapshot: DownloadSnapshot,
        actionType: FlowDownloadActionType,
        deletePartialFileOnCancel: Boolean = false,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        wrapShareInChooser: Boolean = true,
    ): FlowDownloadActionExecutionResult {
        return executeAction(
            context = context,
            snapshot = snapshot,
            actionType = actionType,
            commandExecutor = FlowDownloadClientCommandExecutor(FlowDownload.get(context)),
            deletePartialFileOnCancel = deletePartialFileOnCancel,
            uriResolver = uriResolver,
            wrapShareInChooser = wrapShareInChooser,
        )
    }

    /**
     * 使用指定客户端执行一个动作。
     */
    suspend fun executeAction(
        context: Context,
        client: FlowDownloadClient,
        snapshot: DownloadSnapshot,
        actionType: FlowDownloadActionType,
        deletePartialFileOnCancel: Boolean = false,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        wrapShareInChooser: Boolean = true,
    ): FlowDownloadActionExecutionResult {
        return executeAction(
            context = context,
            snapshot = snapshot,
            actionType = actionType,
            commandExecutor = FlowDownloadClientCommandExecutor(client),
            deletePartialFileOnCancel = deletePartialFileOnCancel,
            uriResolver = uriResolver,
            wrapShareInChooser = wrapShareInChooser,
        )
    }

    /**
     * 使用指定命令执行器或文件动作启动逻辑执行一个动作。
     */
    suspend fun executeAction(
        context: Context,
        snapshot: DownloadSnapshot,
        actionType: FlowDownloadActionType,
        commandExecutor: FlowDownloadCommandExecutor?,
        deletePartialFileOnCancel: Boolean = false,
        uriResolver: FlowDownloadUriResolver = DefaultFlowDownloadUriResolver,
        wrapShareInChooser: Boolean = true,
    ): FlowDownloadActionExecutionResult {
        if (actionType.isCommandAction) {
            if (!isActionAvailable(snapshot, actionType)) {
                return FlowDownloadActionExecutionResult.Unsupported(
                    actionType = actionType,
                    reason = FlowDownloadActionUnsupportedReason.ACTION_NOT_AVAILABLE,
                )
            }

            val resolvedCommandExecutor = commandExecutor ?: return FlowDownloadActionExecutionResult.Unsupported(
                actionType = actionType,
                reason = FlowDownloadActionUnsupportedReason.COMMAND_EXECUTOR_UNAVAILABLE,
            )

            when (actionType) {
                FlowDownloadActionType.PAUSE -> resolvedCommandExecutor.pause(snapshot.id)
                FlowDownloadActionType.RESUME -> resolvedCommandExecutor.resume(snapshot.id)
                FlowDownloadActionType.CANCEL -> resolvedCommandExecutor.cancel(
                    downloadId = snapshot.id,
                    deletePartialFile = deletePartialFileOnCancel,
                )
                FlowDownloadActionType.RETRY -> resolvedCommandExecutor.retry(snapshot.id)
                else -> Unit
            }

            return FlowDownloadActionExecutionResult.CommandDispatched(
                actionType = actionType,
                downloadId = snapshot.id,
            )
        }

        val support = resolveActionSupport(
            context = context,
            snapshot = snapshot,
            actionType = actionType,
            uriResolver = uriResolver,
        )
        if (!support.canExecute || support.intent == null) {
            return FlowDownloadActionExecutionResult.Unsupported(
                actionType = actionType,
                reason = support.unsupportedReason
                    ?: FlowDownloadActionUnsupportedReason.FILE_RESOURCE_UNAVAILABLE,
            )
        }

        return try {
            val launchIntent = support.intent
            context.startActivity(launchIntent)
            FlowDownloadActionExecutionResult.ActivityStarted(
                actionType = actionType,
                intent = launchIntent,
            )
        } catch (error: ActivityNotFoundException) {
            FlowDownloadActionExecutionResult.Unsupported(
                actionType = actionType,
                reason = FlowDownloadActionUnsupportedReason.NO_ACTIVITY_HANDLER,
                message = error.message,
            )
        } catch (error: RuntimeException) {
            FlowDownloadActionExecutionResult.Unsupported(
                actionType = actionType,
                reason = FlowDownloadActionUnsupportedReason.ACTIVITY_START_FAILED,
                message = error.message,
            )
        }
    }

    private fun createFileActionIntent(
        context: Context,
        snapshot: DownloadSnapshot,
        actionType: FlowDownloadActionType,
        resolvedUri: Uri,
        resolvedMimeType: String?,
    ): Intent? {
        if (snapshot.status !is DownloadStatus.Success) {
            return null
        }

        val mimeType = effectiveMimeType(actionType, resolvedMimeType)
        val clipData = ClipData.newUri(
            context.contentResolver,
            snapshot.displayName ?: FileLabelFallback,
            resolvedUri,
        )

        return when (actionType) {
            FlowDownloadActionType.OPEN -> Intent(Intent.ACTION_VIEW)
                .setDataAndType(resolvedUri, mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .apply {
                    this.clipData = clipData
                }

            FlowDownloadActionType.INSTALL -> {
                if (!isInstallable(snapshot)) {
                    return null
                }

                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(resolvedUri, MimeTypeApk)
                    .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .apply {
                        this.clipData = clipData
                    }
            }

            FlowDownloadActionType.SHARE -> Intent(Intent.ACTION_SEND)
                .setType(mimeType)
                .putExtra(Intent.EXTRA_STREAM, resolvedUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .apply {
                    this.clipData = clipData
                }

            else -> throw IllegalArgumentException("Action $actionType is not a file action")
        }
    }

    private fun resolveMimeType(
        context: Context,
        snapshot: DownloadSnapshot,
        resolvedUri: Uri?,
    ): String? {
        normalizeMimeType(snapshot.mimeType)?.let { return it }

        candidateNames(snapshot).firstNotNullOfOrNull(::mimeTypeFromName)?.let { return it }

        if (resolvedUri != null) {
            normalizeMimeType(context.contentResolver.getType(resolvedUri))?.let { return it }
        }

        return null
    }

    private fun effectiveMimeType(
        actionType: FlowDownloadActionType,
        resolvedMimeType: String?,
    ): String {
        return when (actionType) {
            FlowDownloadActionType.INSTALL -> MimeTypeApk
            else -> resolvedMimeType ?: MimeTypeFallback
        }
    }

    private fun candidateNames(snapshot: DownloadSnapshot): Sequence<String> = sequence {
        snapshot.displayName?.takeIf(String::isNotBlank)?.let { displayName ->
            yield(displayName)
        }

        val destinationTail = snapshot.destination
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .takeIf { it.isNotBlank() && !it.startsWith("content://", ignoreCase = true) }
        if (destinationTail != null) {
            yield(destinationTail)
        }
    }

    private fun mimeTypeFromName(name: String): String? {
        val extension = name.substringAfterLast('.', "").lowercase()
        if (extension.isBlank()) {
            return null
        }
        if (extension == "apk") {
            return MimeTypeApk
        }
        return normalizeMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension))
    }

    private fun normalizeMimeType(mimeType: String?): String? {
        return mimeType?.trim()?.takeIf(String::isNotBlank)
    }

    private fun prepareLaunchIntent(
        context: Context,
        actionType: FlowDownloadActionType,
        intent: Intent,
        wrapShareInChooser: Boolean,
    ): Intent {
        if (actionType != FlowDownloadActionType.SHARE || !wrapShareInChooser) {
            return intent
        }

        return Intent.createChooser(
            intent,
            context.getString(R.string.flowdownload_action_share_chooser_title),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun isInstallable(snapshot: DownloadSnapshot): Boolean {
        val mimeType = normalizeMimeType(snapshot.mimeType)
        if (mimeType.equals(MimeTypeApk, ignoreCase = true)) {
            return true
        }

        return candidateNames(snapshot).any { name ->
            name.substringAfterLast('.', "").equals("apk", ignoreCase = true)
        }
    }

    private fun requestCode(
        downloadId: DownloadId,
        actionType: FlowDownloadActionType,
    ): Int {
        val rawHash = downloadId.value.hashCode()
        val normalizedHash = if (rawHash == Int.MIN_VALUE) 0 else abs(rawHash)
        return (normalizedHash * 31) + actionType.ordinal
    }
}
