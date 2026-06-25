package com.ghn.lib.upload

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
 * 描述: 基于 OkHttp 的上传执行引擎，负责真正的网络上传过程。
 */

internal data class UploadHttpResponse(
    val code: Int,
    val body: String?,
)

internal class UploadHttpException(
    val code: Int,
    val responseBody: String?,
    message: String,
) : IOException(message)

internal class OkHttpUploadEngine(
    private val context: Context,
    private val client: OkHttpClient,
    private val config: FlowUploadConfig,
) {

    suspend fun execute(
        request: UploadRequest,
        onProgress: (bytesUploaded: Long, totalBytes: Long?) -> Unit,
        onCallCreated: (Call) -> Unit,
    ): UploadHttpResponse {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .apply {
                request.formFields.forEach { (key, value) ->
                    addFormDataPart(key, value)
                }
                addFormDataPart(
                    request.fieldName,
                    request.fileName,
                    ProgressRequestBody(
                        context = context,
                        source = request.fileUri,
                        mimeType = request.mimeType,
                        onProgress = onProgress
                    )
                )
            }
            .build()
        val okhttpRequest = Request.Builder()
            .url(request.url)
            .apply {
                request.headers.forEach(::addHeader)
                when (request.method) {
                    "POST" -> post(requestBody)
                    "PUT" -> put(requestBody)
                    "PATCH" -> patch(requestBody)
                    else -> throw IllegalArgumentException("Unsupported upload method: ${request.method}")
                }
            }
            .build()
        val call = client.newCall(okhttpRequest)
        onCallCreated(call)
        val response = call.await()
        response.use { actual ->
            val responseBody = actual.body?.string()
                ?.take(config.maxResponseBodyChars)
            if (!actual.isSuccessful) {
                throw UploadHttpException(
                    code = actual.code,
                    responseBody = responseBody,
                    message = "Upload failed with HTTP ${actual.code}"
                )
            }
            return UploadHttpResponse(
                code = actual.code,
                body = responseBody
            )
        }
    }
}

private class ProgressRequestBody(
    private val context: Context,
    private val source: String,
    private val mimeType: String?,
    private val onProgress: (bytesUploaded: Long, totalBytes: Long?) -> Unit,
) : RequestBody() {
    private val contentLengthValue: Long? by lazy {
        UploadSourceResolver.contentLength(context, source)
    }

    override fun contentType() = mimeType?.toMediaTypeOrNull()

    override fun contentLength(): Long = contentLengthValue ?: -1L

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLengthValue
        var uploadedBytes = 0L
        UploadSourceResolver.openInputStream(context, source).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val readSize = input.read(buffer)
                if (readSize == -1) {
                    break
                }
                sink.write(buffer, 0, readSize)
                uploadedBytes += readSize
                onProgress(uploadedBytes, totalBytes)
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun Call.await() = suspendCancellableCoroutine<okhttp3.Response> { continuation ->
    continuation.invokeOnCancellation {
        cancel()
    }
    enqueue(object : okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: okhttp3.Response) {
            continuation.resume(response) {
                response.close()
            }
        }
    })
}
