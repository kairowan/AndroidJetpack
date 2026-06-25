package com.ghn.cocknovel.feature.transfer.web

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.flowdownload.download.model.DownloadId
import com.ghn.lib.download.bridge.toDownloadBridgeCommandOrNull
import com.ghn.lib.download.bridge.toDownloadBridgePayload
import com.ghn.lib.download.bridge.toDownloadBridgeSubscriptionOrNull
import com.ghn.lib.download.bridge.toDownloadTaskRequestOrNull
import com.ghn.lib.base.web.bridge.WebBridgeSubscriptions
import com.ghn.lib.download.DownloadService
import com.ghn.lib.upload.UploadService
import com.ghn.lib.upload.UploadId
import com.ghn.lib.upload.bridge.toUploadBridgeCommandOrNull
import com.ghn.lib.upload.bridge.toUploadBridgePayload
import com.ghn.lib.upload.bridge.toUploadBridgeSubscriptionOrNull
import com.ghn.lib.upload.bridge.toUploadPickerMimeTypes
import com.ghn.lib.upload.bridge.toUploadRequestOrNull
import com.ghn.lib.upload.picker.UploadPickerHostFragment
import com.ghn.lib.base.web.bridge.AnnotatedWebBridgeModule
import com.ghn.lib.base.web.bridge.BridgeHandler
import com.ghn.lib.base.web.bridge.WebBridgeCallback
import com.ghn.lib.base.web.bridge.WebBridgeHost
import com.ghn.routermodule.WebBridgeGroups
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

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
 * 描述: 传输桥接模块，负责为 Web 层提供上传下载相关能力。
 */

@Single(binds = [com.ghn.lib.base.web.bridge.WebBridgeModule::class])
class TransferWebBridgeModule(
    private val downloadService: DownloadService,
    private val uploadService: UploadService,
) : AnnotatedWebBridgeModule() {
    override val group: String = WebBridgeGroups.CORE

    @BridgeHandler("download.enqueue")
    private fun enqueueDownload(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val request = data.toDownloadTaskRequestOrNull()
        if (request == null) {
            callback.failure("download payload invalid")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                downloadService.enqueue(request)
            }.onSuccess { result ->
                callback.success(data = result.toDownloadBridgePayload())
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "download enqueue failed")
            }
        }
    }

    @BridgeHandler("download.getTask")
    private fun getDownloadTask(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val command = data.toDownloadBridgeCommandOrNull()
        if (command == null) {
            callback.failure("download id missing")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                downloadService.get(DownloadId(command.id))
            }.onSuccess { snapshot ->
                if (snapshot == null) {
                    callback.failure("download task not found")
                } else {
                    callback.success(data = snapshot.toDownloadBridgePayload())
                }
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "download query failed")
            }
        }
    }

    @BridgeHandler("download.cancel")
    private fun cancelDownload(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val command = data.toDownloadBridgeCommandOrNull()
        if (command == null) {
            callback.failure("download id missing")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                downloadService.cancel(
                    id = DownloadId(command.id),
                    deletePartialFile = command.deletePartialFile
                )
            }.onSuccess {
                callback.success(
                    data = mapOf(
                        "id" to command.id,
                        "status" to "cancelled"
                    )
                )
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "download cancel failed")
            }
        }
    }

    @BridgeHandler("download.pause")
    private fun pauseDownload(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val command = data.toDownloadBridgeCommandOrNull()
        if (command == null) {
            callback.failure("download id missing")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                downloadService.pause(DownloadId(command.id))
            }.onSuccess {
                callback.success(
                    data = mapOf(
                        "id" to command.id,
                        "status" to "paused"
                    )
                )
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "download pause failed")
            }
        }
    }

    @BridgeHandler("download.resume")
    private fun resumeDownload(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val command = data.toDownloadBridgeCommandOrNull()
        if (command == null) {
            callback.failure("download id missing")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                downloadService.resume(DownloadId(command.id))
            }.onSuccess {
                callback.success(
                    data = mapOf(
                        "id" to command.id,
                        "status" to "queued"
                    )
                )
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "download resume failed")
            }
        }
    }

    @BridgeHandler("download.subscribe")
    private fun subscribeDownload(
        host: WebBridgeHost,
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val request = data.toDownloadBridgeSubscriptionOrNull()
        if (request == null) {
            callback.failure("download subscribe payload invalid")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                val id = DownloadId(request.id)
                // 建立订阅前先读取一次当前快照，保证前端即使晚订阅也能拿到任务现状。
                val snapshot = downloadService.get(id) ?: error("download task not found")
                WebBridgeSubscriptions.subscribe(
                    host = host,
                    channel = DOWNLOAD_CHANNEL,
                    taskId = request.id,
                    callbackHandler = request.callbackHandler,
                    snapshots = downloadService.observe(id)
                ) { latestSnapshot ->
                    mapOf(
                        "channel" to DOWNLOAD_CHANNEL,
                        "event" to "snapshot",
                        "id" to latestSnapshot.id.value,
                        "task" to latestSnapshot.toDownloadBridgePayload()
                    )
                }
                snapshot
            }.onSuccess { snapshot ->
                callback.success(
                    data = mapOf(
                        "id" to request.id,
                        "callbackHandler" to request.callbackHandler,
                        "status" to "subscribed",
                        "task" to snapshot.toDownloadBridgePayload()
                    )
                )
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "download subscribe failed")
            }
        }
    }

    @BridgeHandler("download.unsubscribe")
    private fun unsubscribeDownload(
        host: WebBridgeHost,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val request = data.toDownloadBridgeSubscriptionOrNull()
        if (request == null) {
            callback.failure("download unsubscribe payload invalid")
            return
        }
        val removed = WebBridgeSubscriptions.unsubscribe(
            host = host,
            channel = DOWNLOAD_CHANNEL,
            taskId = request.id,
            callbackHandler = request.callbackHandler
        )
        callback.success(
            data = mapOf(
                "id" to request.id,
                "callbackHandler" to request.callbackHandler,
                "removed" to removed
            )
        )
    }

    @BridgeHandler("upload.enqueue")
    private fun enqueueUpload(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val request = data.toUploadRequestOrNull()
        if (request == null) {
            callback.failure("upload payload invalid")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                uploadService.enqueue(request)
            }.onSuccess { uploadId ->
                callback.success(
                    data = mapOf(
                        "id" to uploadId.value,
                        "status" to "queued"
                    )
                )
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "upload enqueue failed")
            }
        }
    }

    @BridgeHandler("upload.pickImage")
    private fun pickImage(
        activity: FragmentActivity,
        callback: WebBridgeCallback,
    ) {
        activity.lifecycleScope.launch {
            runCatching {
                // 选择器能力已经下沉到上传库，Bridge 这里只负责协议转换和回调透传。
                UploadPickerHostFragment.obtain(activity).pickImage()
            }.onSuccess { pickedFile ->
                if (pickedFile == null) {
                    callback.failure("image picker cancelled", code = PICKER_CANCELLED_CODE)
                } else {
                    callback.success(data = pickedFile.toUploadBridgePayload())
                }
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "image picker failed")
            }
        }
    }

    @BridgeHandler("upload.pickFile")
    private fun pickFile(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        activity.lifecycleScope.launch {
            runCatching {
                UploadPickerHostFragment.obtain(activity).pickFile(data.toUploadPickerMimeTypes())
            }.onSuccess { pickedFile ->
                if (pickedFile == null) {
                    callback.failure("file picker cancelled", code = PICKER_CANCELLED_CODE)
                } else {
                    callback.success(data = pickedFile.toUploadBridgePayload())
                }
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "file picker failed")
            }
        }
    }

    @BridgeHandler("upload.getTask")
    private fun getUploadTask(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val command = data.toUploadBridgeCommandOrNull()
        if (command == null) {
            callback.failure("upload id missing")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                uploadService.get(UploadId(command.id))
            }.onSuccess { snapshot ->
                if (snapshot == null) {
                    callback.failure("upload task not found")
                } else {
                    callback.success(data = snapshot.toUploadBridgePayload())
                }
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "upload query failed")
            }
        }
    }

    @BridgeHandler("upload.cancel")
    private fun cancelUpload(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val command = data.toUploadBridgeCommandOrNull()
        if (command == null) {
            callback.failure("upload id missing")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                uploadService.cancel(UploadId(command.id))
            }.onSuccess {
                callback.success(
                    data = mapOf(
                        "id" to command.id,
                        "status" to "cancelled"
                    )
                )
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "upload cancel failed")
            }
        }
    }

    @BridgeHandler("upload.subscribe")
    private fun subscribeUpload(
        host: WebBridgeHost,
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val request = data.toUploadBridgeSubscriptionOrNull()
        if (request == null) {
            callback.failure("upload subscribe payload invalid")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                val id = UploadId(request.id)
                val snapshot = uploadService.get(id) ?: error("upload task not found")
                WebBridgeSubscriptions.subscribe(
                    host = host,
                    channel = UPLOAD_CHANNEL,
                    taskId = request.id,
                    callbackHandler = request.callbackHandler,
                    snapshots = uploadService.observe(id)
                ) { latestSnapshot ->
                    mapOf(
                        "channel" to UPLOAD_CHANNEL,
                        "event" to "snapshot",
                        "id" to latestSnapshot.id.value,
                        "task" to latestSnapshot.toUploadBridgePayload()
                    )
                }
                snapshot
            }.onSuccess { snapshot ->
                callback.success(
                    data = mapOf(
                        "id" to request.id,
                        "callbackHandler" to request.callbackHandler,
                        "status" to "subscribed",
                        "task" to snapshot.toUploadBridgePayload()
                    )
                )
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "upload subscribe failed")
            }
        }
    }

    @BridgeHandler("upload.unsubscribe")
    private fun unsubscribeUpload(
        host: WebBridgeHost,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val request = data.toUploadBridgeSubscriptionOrNull()
        if (request == null) {
            callback.failure("upload unsubscribe payload invalid")
            return
        }
        val removed = WebBridgeSubscriptions.unsubscribe(
            host = host,
            channel = UPLOAD_CHANNEL,
            taskId = request.id,
            callbackHandler = request.callbackHandler
        )
        callback.success(
            data = mapOf(
                "id" to request.id,
                "callbackHandler" to request.callbackHandler,
                "removed" to removed
            )
        )
    }

    @BridgeHandler("upload.retry")
    private fun retryUpload(
        activity: FragmentActivity,
        data: String?,
        callback: WebBridgeCallback,
    ) {
        val command = data.toUploadBridgeCommandOrNull()
        if (command == null) {
            callback.failure("upload id missing")
            return
        }
        activity.lifecycleScope.launch {
            runCatching {
                uploadService.retry(UploadId(command.id))
            }.onSuccess {
                callback.success(
                    data = mapOf(
                        "id" to command.id,
                        "status" to "queued"
                    )
                )
            }.onFailure { throwable ->
                callback.failure(throwable.message ?: "upload retry failed")
            }
        }
    }
}

private const val PICKER_CANCELLED_CODE = -2
private const val DOWNLOAD_CHANNEL = "download"
private const val UPLOAD_CHANNEL = "upload"
