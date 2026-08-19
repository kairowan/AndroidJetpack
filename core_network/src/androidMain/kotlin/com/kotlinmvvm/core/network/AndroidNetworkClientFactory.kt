package com.kotlinmvvm.core.network

import android.content.Context
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

fun createAndroidNetworkClient(
    context: Context,
    config: NetworkConfig = NetworkConfig(),
    interceptors: List<NetworkInterceptor> = emptyList()
): NetworkClient = NetworkClient(
    engine = OkHttpNetworkEngine(context.applicationContext),
    config = config,
    interceptors = interceptors
)

private class OkHttpNetworkEngine(
    context: Context
) : NetworkEngine {
    private val client = OkHttpClient.Builder()
        // ponytail: commonMain owns the total attempt timeout; Call.cancel() stops OkHttp.
        .connectTimeout(0, TimeUnit.MILLISECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(false)
        .cache(Cache(File(context.cacheDir, CACHE_DIRECTORY), CACHE_MAX_BYTES))
        .build()

    override suspend fun execute(request: NetworkRequest): NetworkResponse =
        suspendCancellableCoroutine { continuation ->
            val nativeRequest = try {
                request.toOkHttpRequest()
            } catch (_: IllegalArgumentException) {
                continuation.resumeWithException(NetworkInvalidUrlException(request.url))
                return@suspendCancellableCoroutine
            }
            val call = client.newCall(nativeRequest)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                NetworkTransportException(
                                    requestUrl = request.url,
                                    detail = e.message ?: "OkHttp call failed",
                                    cause = e
                                )
                            )
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (!continuation.isActive) {
                            response.close()
                            return
                        }
                        try {
                            response.use {
                                if (
                                    request.cachePolicy == NetworkCachePolicy.CACHE_ONLY &&
                                    it.code == CACHE_MISS_STATUS &&
                                    it.cacheResponse == null &&
                                    it.networkResponse == null
                                ) {
                                    continuation.resumeWithException(
                                        NetworkCacheMissException(request.url)
                                    )
                                    return@use
                                }
                                continuation.resume(
                                    NetworkResponse(
                                        request = request,
                                        statusCode = it.code,
                                        headers = it.headers.toMultimap(),
                                        body = it.body.bytes(),
                                        source = it.toNetworkResponseSource(request.cachePolicy)
                                    )
                                )
                            }
                        } catch (error: Exception) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    NetworkTransportException(
                                        requestUrl = request.url,
                                        detail = error.message ?: "Unable to read response",
                                        cause = error
                                    )
                                )
                            }
                        }
                    }
                }
            )
        }

    private fun Response.toNetworkResponseSource(
        cachePolicy: NetworkCachePolicy
    ): NetworkResponseSource =
        when {
            cachePolicy == NetworkCachePolicy.CACHE_ONLY -> NetworkResponseSource.CACHE
            cachePolicy == NetworkCachePolicy.NETWORK_ONLY ||
                cachePolicy == NetworkCachePolicy.NO_STORE -> NetworkResponseSource.NETWORK

            cacheResponse != null && networkResponse == null -> NetworkResponseSource.CACHE
            networkResponse != null -> NetworkResponseSource.NETWORK
            else -> NetworkResponseSource.UNKNOWN
        }

    private fun NetworkRequest.toOkHttpRequest(): Request {
        val builder = Request.Builder().url(url)
        headers.forEach { (name, value) -> builder.header(name, value) }
        when (cachePolicy) {
            NetworkCachePolicy.DEFAULT -> Unit
            NetworkCachePolicy.NETWORK_ONLY -> builder.cacheControl(CacheControl.FORCE_NETWORK)
            NetworkCachePolicy.CACHE_ONLY -> builder.cacheControl(CacheControl.FORCE_CACHE)
            NetworkCachePolicy.NO_STORE -> builder.cacheControl(
                CacheControl.Builder().noCache().noStore().build()
            )
        }

        val contentType = headers.entries
            .firstOrNull { (name, _) -> name.equals("Content-Type", ignoreCase = true) }
            ?.value
            ?.toMediaTypeOrNull()
        val requestBody = body?.content?.toRequestBody(contentType)
        when (method) {
            NetworkMethod.GET -> builder.get()
            NetworkMethod.HEAD -> builder.head()
            NetworkMethod.POST -> builder.post(requestBody ?: EMPTY_BODY)
            NetworkMethod.PUT -> builder.put(requestBody ?: EMPTY_BODY)
            NetworkMethod.PATCH -> builder.patch(requestBody ?: EMPTY_BODY)
            NetworkMethod.DELETE -> {
                if (requestBody == null) builder.delete() else builder.delete(requestBody)
            }
            NetworkMethod.OPTIONS -> builder.method("OPTIONS", requestBody)
        }
        return builder.build()
    }

    private companion object {
        const val CACHE_MISS_STATUS = 504
        const val CACHE_DIRECTORY = "http_cache"
        const val CACHE_MAX_BYTES = 50L * 1024 * 1024
        val EMPTY_BODY = ByteArray(0).toRequestBody()
    }
}
