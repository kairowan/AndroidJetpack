package com.example.flowdownload.download.network

import com.example.flowdownload.download.model.Clock
import com.example.flowdownload.download.model.DownloadFailure
import com.example.flowdownload.download.model.DownloadFailureCategory
import com.example.flowdownload.download.model.PersistedDownload
import com.example.flowdownload.download.storage.DownloadDestinationAccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import okhttp3.ResponseBody
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载网络引擎共享传输支持，统一处理断点续传头构建、文件写盘、校验收尾与错误分类。
 */
internal object DownloadTransferSupport {
    fun buildRequestHeaders(
        record: PersistedDownload,
        resumeFromBytes: Long,
    ): Map<String, String> {
        val headers = linkedMapOf<String, String>()
        headers.putAll(record.headers)

        if (resumeFromBytes > 0L) {
            headers["Range"] = "bytes=$resumeFromBytes-"
            when {
                !record.eTag.isNullOrBlank() -> headers["If-Range"] = record.eTag
                !record.lastModified.isNullOrBlank() -> headers["If-Range"] = record.lastModified
            }
        }

        return headers
    }

    suspend fun downloadBody(
        clock: Clock,
        progressThrottleMs: Long,
        body: ResponseBody?,
        tempFile: File,
        initialBytes: Long,
        totalBytes: Long?,
        eTag: String?,
        lastModified: String?,
        mimeType: String?,
        stopController: DownloadStopController,
        append: Boolean,
        destination: String,
        displayName: String?,
        overwriteExisting: Boolean,
        destinationAccess: DownloadDestinationAccess,
        expectedSha256: String?,
        onProgress: suspend (DownloadProgressSample) -> Unit,
    ): DownloadTerminalResult {
        val destinationLabel = displayName?.takeIf(String::isNotBlank)
            ?: destination.substringAfterLast('/').ifBlank { destination }
        val responseBody = body ?: return failedResult(
            category = DownloadFailureCategory.NETWORK_UNAVAILABLE,
            message = "Empty response body while downloading $destinationLabel",
            retryable = true,
            bytesDownloaded = initialBytes,
            totalBytes = totalBytes,
            eTag = eTag,
            lastModified = lastModified,
        )

        if (!append) {
            try {
                resetTempFile(tempFile)
            } catch (error: IOException) {
                return failedStorageResult(
                    error = error,
                    fallbackMessage = "Failed to prepare ${tempFile.absolutePath}",
                    bytesDownloaded = initialBytes,
                    totalBytes = totalBytes,
                    eTag = eTag,
                    lastModified = lastModified,
                )
            }
        }

        var lastEmissionAt = 0L
        var bytesDownloaded = initialBytes
        val speedWindowStartedAt = clock.now()
        val speedWindowStartedBytes = initialBytes

        onProgress(
            DownloadProgressSample(
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes,
                bytesPerSecond = null,
                updatedAtEpochMs = clock.now(),
                eTag = eTag,
                lastModified = lastModified,
                mimeType = mimeType,
            ),
        )

        try {
            responseBody.byteStream().use { input ->
                FileOutputStream(tempFile, append).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                    while (true) {
                        coroutineContext.ensureActive()
                        throwIfStopped(stopController)

                        val read = input.read(buffer)
                        if (read < 0) break

                        output.write(buffer, 0, read)
                        bytesDownloaded += read

                        val now = clock.now()
                        if (lastEmissionAt == 0L || now - lastEmissionAt >= progressThrottleMs) {
                            val elapsed = (now - speedWindowStartedAt).coerceAtLeast(1L)
                            val bytesPerSecond =
                                ((bytesDownloaded - speedWindowStartedBytes) * 1000L) / elapsed

                            onProgress(
                                DownloadProgressSample(
                                    bytesDownloaded = bytesDownloaded,
                                    totalBytes = totalBytes,
                                    bytesPerSecond = bytesPerSecond,
                                    updatedAtEpochMs = now,
                                    eTag = eTag,
                                    lastModified = lastModified,
                                    mimeType = mimeType,
                                ),
                            )
                            lastEmissionAt = now
                        }
                    }

                    output.flush()
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            if (!coroutineContext.isActive || stopController.reason != null) {
                throw CancellationException("Download stop requested", error)
            }
            return failedResult(
                category = classifyThrowable(error),
                message = error.message ?: "Failed while streaming response body",
                retryable = isRetryable(error),
                bytesDownloaded = tempFile.length(),
                totalBytes = totalBytes,
                eTag = eTag,
                lastModified = lastModified,
            )
        }

        if (expectedSha256 != null) {
            val actualSha256 = sha256(tempFile)
            if (!expectedSha256.equals(actualSha256, ignoreCase = true)) {
                tempFile.delete()
                return failedResult(
                    category = DownloadFailureCategory.CHECKSUM_MISMATCH,
                    message = "Checksum mismatch for $destinationLabel",
                    retryable = false,
                    bytesDownloaded = bytesDownloaded,
                    totalBytes = totalBytes,
                    eTag = eTag,
                    lastModified = lastModified,
                )
            }
        }

        return try {
            destinationAccess.finalizeDownload(
                tempFile = tempFile,
                destination = destination,
                overwriteExisting = overwriteExisting,
            )
            onProgress(
                DownloadProgressSample(
                    bytesDownloaded = bytesDownloaded,
                    totalBytes = totalBytes ?: bytesDownloaded,
                    bytesPerSecond = 0L,
                    updatedAtEpochMs = clock.now(),
                    eTag = eTag,
                    lastModified = lastModified,
                    mimeType = mimeType,
                ),
            )
            DownloadTerminalResult.Success(
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes ?: bytesDownloaded,
                eTag = eTag,
                lastModified = lastModified,
                mimeType = mimeType,
            )
        } catch (error: IOException) {
            failedStorageResult(
                error = error,
                fallbackMessage = "Failed while finalizing download",
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes,
                eTag = eTag,
                lastModified = lastModified,
            )
        }
    }

    fun resetTempFile(tempFile: File) {
        ensureParentDirectory(tempFile)
        if (tempFile.exists()) {
            FileOutputStream(tempFile, false).use { }
            return
        }

        if (!tempFile.createNewFile() && !tempFile.exists()) {
            throw IOException("Failed to create file ${tempFile.absolutePath}")
        }
    }

    fun parseCompleteLength(contentRangeHeader: String?): Long? {
        val contentRange = contentRangeHeader ?: return null
        val slashIndex = contentRange.lastIndexOf('/')
        if (slashIndex < 0 || slashIndex == contentRange.lastIndex) return null
        return contentRange.substring(slashIndex + 1).toLongOrNull()
    }

    fun classifyHttpFailure(code: Int): DownloadFailureCategory {
        return when (code) {
            401 -> DownloadFailureCategory.UNAUTHORIZED
            403 -> DownloadFailureCategory.FORBIDDEN
            404 -> DownloadFailureCategory.NOT_FOUND
            416 -> DownloadFailureCategory.RANGE_NOT_SATISFIABLE
            in 400..499 -> DownloadFailureCategory.HTTP_4XX
            in 500..599 -> DownloadFailureCategory.HTTP_5XX
            else -> DownloadFailureCategory.UNKNOWN
        }
    }

    fun classifyThrowable(error: Throwable): DownloadFailureCategory {
        return when {
            error is SocketTimeoutException -> DownloadFailureCategory.TIMEOUT
            error is FileNotFoundException -> DownloadFailureCategory.STORAGE_UNAVAILABLE
            error is IOException && isDiskFull(error) -> DownloadFailureCategory.DISK_FULL
            error is IOException && isStorageAccessError(error) ->
                DownloadFailureCategory.STORAGE_UNAVAILABLE

            error is IOException -> DownloadFailureCategory.NETWORK_UNAVAILABLE
            else -> DownloadFailureCategory.UNKNOWN
        }
    }

    fun isRetryable(error: Throwable): Boolean {
        return when {
            error is SocketTimeoutException -> true
            error is FileNotFoundException -> false
            error is IOException && isDiskFull(error) -> false
            error is IOException && isStorageAccessError(error) -> false
            error is IOException -> true
            else -> false
        }
    }

    fun failedStorageResult(
        error: IOException,
        fallbackMessage: String,
        bytesDownloaded: Long,
        totalBytes: Long?,
        eTag: String?,
        lastModified: String?,
    ): DownloadTerminalResult.Failed {
        val category = classifyStorageThrowable(error)
        return failedResult(
            category = category,
            message = error.message ?: fallbackMessage,
            retryable = category == DownloadFailureCategory.STORAGE_UNAVAILABLE,
            bytesDownloaded = bytesDownloaded,
            totalBytes = totalBytes,
            eTag = eTag,
            lastModified = lastModified,
        )
    }

    fun failedResult(
        category: DownloadFailureCategory,
        message: String,
        retryable: Boolean,
        bytesDownloaded: Long,
        totalBytes: Long?,
        eTag: String?,
        lastModified: String?,
    ): DownloadTerminalResult.Failed {
        return DownloadTerminalResult.Failed(
            failure = DownloadFailure(
                category = category,
                message = message,
                retryable = retryable,
            ),
            bytesDownloaded = bytesDownloaded,
            totalBytes = totalBytes,
            eTag = eTag,
            lastModified = lastModified,
        )
    }

    fun throwIfStopped(stopController: DownloadStopController) {
        if (stopController.reason != null) {
            throw CancellationException("Download stop requested")
        }
    }

    private fun ensureParentDirectory(file: File) {
        val parent = file.parentFile ?: return
        if (!parent.exists() && !parent.mkdirs()) {
            throw IOException("Failed to create directory ${parent.absolutePath}")
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }

    private fun classifyStorageThrowable(error: IOException): DownloadFailureCategory {
        return when {
            isDiskFull(error) -> DownloadFailureCategory.DISK_FULL
            error.message?.contains("already exists", ignoreCase = true) == true ->
                DownloadFailureCategory.FILE_CONFLICT

            else -> DownloadFailureCategory.STORAGE_UNAVAILABLE
        }
    }

    private fun isDiskFull(error: IOException): Boolean {
        val message = error.message.orEmpty()
        return message.contains("ENOSPC", ignoreCase = true) ||
            message.contains("No space left", ignoreCase = true)
    }

    private fun isStorageAccessError(error: IOException): Boolean {
        val message = error.message.orEmpty()
        return message.contains("Permission denied", ignoreCase = true) ||
            message.contains("Access is denied", ignoreCase = true) ||
            message.contains("EACCES", ignoreCase = true) ||
            message.contains("EROFS", ignoreCase = true)
    }
}
