package com.example.flowdownload.download.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Room 下载表实体，保存断点续传、优先级调度和错误恢复所需的完整记录。
 */
@Entity(
    tableName = "downloads",
    indices = [
        Index(value = ["url"]),
        Index(value = ["destination"]),
        Index(value = ["tag"]),
        Index(value = ["groupName"]),
        Index(value = ["status"]),
    ],
)
data class DownloadEntity(
    @PrimaryKey val id: String,
    val url: String,
    val destination: String,
    val tempDestination: String,
    val displayName: String?,
    val tag: String?,
    val groupName: String?,
    val extrasBlob: String,
    val networkType: String,
    val requiresCharging: Boolean,
    val requiresBatteryNotLow: Boolean,
    val requiresStorageNotLow: Boolean,
    val requiresDeviceIdle: Boolean,
    val priority: Int,
    val status: String,
    val bytesDownloaded: Long,
    val totalBytes: Long?,
    val eTag: String?,
    val lastModified: String?,
    val mimeType: String?,
    val errorCategory: String?,
    val errorMessage: String?,
    val retryable: Boolean,
    val retryCount: Int,
    val maxRetries: Int,
    val nextAttemptAtEpochMs: Long?,
    val headersBlob: String,
    val expectedSha256: String?,
    val overwriteExisting: Boolean,
    val pendingStopReason: String?,
    val deletePartialOnCancel: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val bytesPerSecond: Long?,
)
