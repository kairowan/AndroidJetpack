package com.ghn.lib.upload

import java.util.UUID

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
 * 描述: 上传相关模型定义，统一描述请求、状态、快照与事件结构。
 */

@JvmInline
value class UploadId(val value: String) {
    companion object {
        fun newId(): UploadId = UploadId(UUID.randomUUID().toString())
    }
}

data class UploadRequest(
    val id: UploadId = UploadId.newId(),
    val url: String,
    val fileUri: String,
    val fileName: String? = null,
    val fieldName: String = "file",
    val mimeType: String? = null,
    val method: String = "POST",
    val headers: Map<String, String> = emptyMap(),
    val formFields: Map<String, String> = emptyMap(),
    val tag: String? = null,
    val group: String? = null,
    val extras: Map<String, String> = emptyMap(),
    val maxRetries: Int = 1,
    val priority: Int = 0,
)

enum class UploadStatus {
    QUEUED,
    RETRY_WAITING,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED,
}

data class UploadSnapshot(
    val id: UploadId,
    val url: String,
    val fileUri: String,
    val fileName: String,
    val fieldName: String,
    val mimeType: String?,
    val method: String,
    val tag: String?,
    val group: String?,
    val extras: Map<String, String>,
    val status: UploadStatus,
    val bytesUploaded: Long,
    val totalBytes: Long?,
    val progressPercent: Int?,
    val responseCode: Int?,
    val responseBody: String?,
    val errorMessage: String?,
    val retryCount: Int,
    val maxRetries: Int,
    val priority: Int,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
) {
    val progressFraction: Float?
        get() = progressPercent?.div(100f)
}

sealed interface UploadEvent {
    val uploadId: UploadId

    data class Enqueued(
        override val uploadId: UploadId,
        val snapshot: UploadSnapshot,
    ) : UploadEvent

    data class Started(
        override val uploadId: UploadId,
        val snapshot: UploadSnapshot,
    ) : UploadEvent

    data class Progress(
        override val uploadId: UploadId,
        val snapshot: UploadSnapshot,
    ) : UploadEvent

    data class Success(
        override val uploadId: UploadId,
        val snapshot: UploadSnapshot,
    ) : UploadEvent

    data class Failed(
        override val uploadId: UploadId,
        val snapshot: UploadSnapshot,
    ) : UploadEvent

    data class Cancelled(
        override val uploadId: UploadId,
        val snapshot: UploadSnapshot,
    ) : UploadEvent
}
