package com.example.flowdownload.download.network

import com.example.flowdownload.download.model.Clock
import com.example.flowdownload.download.model.DownloadFailure
import com.example.flowdownload.download.model.DownloadFailureCategory
import com.example.flowdownload.download.model.PersistedDownload
import com.example.flowdownload.download.storage.DownloadDestinationAccess
import com.example.flowdownload.download.storage.LocalFileDownloadDestinationAccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.io.IOException
import kotlin.coroutines.coroutineContext

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 基于 Retrofit 的下载引擎，负责在声明式接口层之上复用流式下载、断点续传和文件校验能力。
 */
class RetrofitDownloadEngine(
    private val clock: Clock,
    retrofit: Retrofit,
    private val progressThrottleMs: Long = 200L,
    private val destinationAccess: DownloadDestinationAccess = LocalFileDownloadDestinationAccess,
) : HttpDownloadEngine {
    private val service = retrofit.create(RetrofitDownloadApi::class.java)

    private companion object {
        const val HTTP_RANGE_NOT_SATISFIABLE = 416
    }

    override suspend fun run(
        record: PersistedDownload,
        stopController: DownloadStopController,
        onProgress: suspend (DownloadProgressSample) -> Unit,
    ): DownloadTerminalResult = withContext(Dispatchers.IO) {
        val tempFile = File(record.tempDestination)

        try {
            destinationAccess.prepareDestination(record.destination, tempFile)
        } catch (error: IOException) {
            return@withContext DownloadTransferSupport.failedStorageResult(
                error = error,
                fallbackMessage = "Failed to prepare ${record.destination}",
                bytesDownloaded = record.bytesDownloaded,
                totalBytes = record.totalBytes,
                eTag = record.eTag,
                lastModified = record.lastModified,
            )
        }

        if (destinationAccess.exists(record.destination) &&
            !record.overwriteExisting &&
            tempFile.length() == 0L
        ) {
            return@withContext DownloadTerminalResult.Failed(
                failure = DownloadFailure(
                    category = DownloadFailureCategory.FILE_CONFLICT,
                    message = "Destination already exists: ${record.destination}",
                    retryable = false,
                ),
                bytesDownloaded = record.bytesDownloaded,
                totalBytes = record.totalBytes,
                eTag = record.eTag,
                lastModified = record.lastModified,
            )
        }

        var forceRestartFromZero = false

        while (true) {
            coroutineContext.ensureActive()
            DownloadTransferSupport.throwIfStopped(stopController)

            val localBytes = try {
                if (forceRestartFromZero) {
                    DownloadTransferSupport.resetTempFile(tempFile)
                    0L
                } else {
                    tempFile.length()
                }
            } catch (error: IOException) {
                return@withContext DownloadTransferSupport.failedStorageResult(
                    error = error,
                    fallbackMessage = "Failed to reset ${tempFile.absolutePath}",
                    bytesDownloaded = tempFile.length(),
                    totalBytes = record.totalBytes,
                    eTag = record.eTag,
                    lastModified = record.lastModified,
                )
            }

            try {
                val response = service.download(
                    url = record.url,
                    headers = DownloadTransferSupport.buildRequestHeaders(record, localBytes),
                )

                val responseCode = response.code()
                val requestedResume = localBytes > 0L
                val body = response.body() ?: response.errorBody()
                val eTag = response.headers()["ETag"] ?: record.eTag
                val lastModified = response.headers()["Last-Modified"] ?: record.lastModified
                val mimeType = body?.contentType()?.toString() ?: record.mimeType
                val responseLength = body?.contentLength()?.takeIf { it >= 0L }
                val totalBytes = when {
                    responseCode == 206 -> responseLength?.plus(localBytes)
                    responseCode in 200..299 -> responseLength
                    else -> record.totalBytes
                }

                when {
                    responseCode == 206 -> {
                        return@withContext DownloadTransferSupport.downloadBody(
                            clock = clock,
                            progressThrottleMs = progressThrottleMs,
                            body = body,
                            tempFile = tempFile,
                            initialBytes = localBytes,
                            totalBytes = totalBytes,
                            eTag = eTag,
                            lastModified = lastModified,
                            mimeType = mimeType,
                            stopController = stopController,
                            append = true,
                            destination = record.destination,
                            displayName = record.displayName,
                            overwriteExisting = record.overwriteExisting,
                            destinationAccess = destinationAccess,
                            expectedSha256 = record.expectedSha256,
                            onProgress = onProgress,
                        )
                    }

                    responseCode == 200 && requestedResume -> {
                        forceRestartFromZero = true
                        continue
                    }

                    responseCode == 200 -> {
                        return@withContext DownloadTransferSupport.downloadBody(
                            clock = clock,
                            progressThrottleMs = progressThrottleMs,
                            body = body,
                            tempFile = tempFile,
                            initialBytes = 0L,
                            totalBytes = totalBytes,
                            eTag = eTag,
                            lastModified = lastModified,
                            mimeType = mimeType,
                            stopController = stopController,
                            append = false,
                            destination = record.destination,
                            displayName = record.displayName,
                            overwriteExisting = record.overwriteExisting,
                            destinationAccess = destinationAccess,
                            expectedSha256 = record.expectedSha256,
                            onProgress = onProgress,
                        )
                    }

                    responseCode == HTTP_RANGE_NOT_SATISFIABLE -> {
                        val serverBytes = DownloadTransferSupport.parseCompleteLength(
                            response.headers()["Content-Range"],
                        )
                        if (localBytes > 0L && serverBytes != null && localBytes == serverBytes) {
                            try {
                                destinationAccess.finalizeDownload(
                                    tempFile = tempFile,
                                    destination = record.destination,
                                    overwriteExisting = record.overwriteExisting,
                                )
                            } catch (error: IOException) {
                                return@withContext DownloadTransferSupport.failedStorageResult(
                                    error = error,
                                    fallbackMessage = "Failed to finalize ${record.destination}",
                                    bytesDownloaded = localBytes,
                                    totalBytes = serverBytes,
                                    eTag = eTag,
                                    lastModified = lastModified,
                                )
                            }
                            return@withContext DownloadTerminalResult.Success(
                                bytesDownloaded = serverBytes,
                                totalBytes = serverBytes,
                                eTag = eTag,
                                lastModified = lastModified,
                                mimeType = mimeType,
                            )
                        }

                        if (forceRestartFromZero) {
                            return@withContext DownloadTransferSupport.failedResult(
                                category = DownloadFailureCategory.RANGE_NOT_SATISFIABLE,
                                message = "Server rejected resumed range request",
                                retryable = false,
                                bytesDownloaded = localBytes,
                                totalBytes = serverBytes ?: record.totalBytes,
                                eTag = eTag,
                                lastModified = lastModified,
                            )
                        }

                        forceRestartFromZero = true
                        continue
                    }

                    responseCode in 200..299 -> {
                        return@withContext DownloadTransferSupport.downloadBody(
                            clock = clock,
                            progressThrottleMs = progressThrottleMs,
                            body = body,
                            tempFile = tempFile,
                            initialBytes = 0L,
                            totalBytes = totalBytes,
                            eTag = eTag,
                            lastModified = lastModified,
                            mimeType = mimeType,
                            stopController = stopController,
                            append = false,
                            destination = record.destination,
                            displayName = record.displayName,
                            overwriteExisting = record.overwriteExisting,
                            destinationAccess = destinationAccess,
                            expectedSha256 = record.expectedSha256,
                            onProgress = onProgress,
                        )
                    }

                    else -> {
                        return@withContext DownloadTransferSupport.failedResult(
                            category = DownloadTransferSupport.classifyHttpFailure(responseCode),
                            message = "HTTP $responseCode while downloading ${record.url}",
                            retryable = responseCode >= 500,
                            bytesDownloaded = localBytes,
                            totalBytes = totalBytes,
                            eTag = eTag,
                            lastModified = lastModified,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                if (!coroutineContext.isActive || stopController.reason != null) {
                    throw CancellationException("Download stop requested", error)
                }
                return@withContext DownloadTransferSupport.failedResult(
                    category = DownloadTransferSupport.classifyThrowable(error),
                    message = error.message ?: "Unknown download error",
                    retryable = DownloadTransferSupport.isRetryable(error),
                    bytesDownloaded = tempFile.length(),
                    totalBytes = record.totalBytes,
                    eTag = record.eTag,
                    lastModified = record.lastModified,
                )
            }
        }

        DownloadTransferSupport.failedResult(
            category = DownloadFailureCategory.UNKNOWN,
            message = "Download loop exited unexpectedly",
            retryable = true,
            bytesDownloaded = tempFile.length(),
            totalBytes = record.totalBytes,
            eTag = record.eTag,
            lastModified = record.lastModified,
        )
    }

    private interface RetrofitDownloadApi {
        @Streaming
        @GET
        suspend fun download(
            @Url url: String,
            @HeaderMap headers: Map<String, String>,
        ): Response<ResponseBody>
    }
}
