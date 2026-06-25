package com.example.flowdownload

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.flowdownload.download.model.DownloadId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载命令广播接收器，承接通知按钮触发的暂停、恢复、取消和重试等宿主外部动作。
 */
class FlowDownloadCommandReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != ACTION_EXECUTE_COMMAND) {
            return
        }

        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                handle(
                    context = context.applicationContext,
                    intent = intent,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handle(
        context: Context,
        intent: Intent,
    ) {
        val actionType = intent.getStringExtra(EXTRA_ACTION_TYPE)
            ?.let { runCatching { FlowDownloadActionType.valueOf(it) }.getOrNull() }
            ?: return
        if (!actionType.isCommandAction) {
            return
        }

        val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
            ?.takeIf(String::isNotBlank)
            ?.let(::DownloadId)
            ?: return

        val client = FlowDownload.ensureClient(
            context = context,
            config = FlowDownloadConfigIntentCodec.decode(intent),
        )

        when (actionType) {
            FlowDownloadActionType.PAUSE -> client.pause(downloadId)
            FlowDownloadActionType.RESUME -> client.resume(downloadId)
            FlowDownloadActionType.CANCEL -> client.cancel(downloadId, deletePartialFile = false)
            FlowDownloadActionType.RETRY -> client.retry(downloadId)
            else -> Unit
        }
    }

    companion object {
        internal const val ACTION_EXECUTE_COMMAND =
            "com.example.flowdownload.action.EXECUTE_COMMAND"
        internal const val EXTRA_ACTION_TYPE = "flowdownload.extra.ACTION_TYPE"
        internal const val EXTRA_DOWNLOAD_ID = "flowdownload.extra.DOWNLOAD_ID"

        private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        internal fun createIntent(
            context: Context,
            config: FlowDownloadConfig,
            downloadId: DownloadId,
            actionType: FlowDownloadActionType,
        ): Intent {
            require(actionType.isCommandAction) {
                "Action $actionType is not a command action"
            }

            val intent = Intent(context, FlowDownloadCommandReceiver::class.java)
                .setAction(ACTION_EXECUTE_COMMAND)
                .putExtra(EXTRA_ACTION_TYPE, actionType.name)
                .putExtra(EXTRA_DOWNLOAD_ID, downloadId.value)

            return FlowDownloadConfigIntentCodec.encode(intent, config)
        }
    }
}
