@file:OptIn(
    kotlinx.cinterop.BetaInteropApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class
)

package com.kotlinmvvm.core.network

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorTimedOut
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.Foundation.NSURLRequestReturnCacheDataDontLoad
import platform.Foundation.NSURLRequestUseProtocolCachePolicy
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.posix.memcpy

fun createIosNetworkClient(
    config: NetworkConfig = NetworkConfig(),
    interceptors: List<NetworkInterceptor> = emptyList()
): NetworkClient = NetworkClient(
    engine = FoundationNetworkEngine(config),
    config = config,
    interceptors = interceptors
)

private fun ByteArray.toNSData(): NSData =
    if (isEmpty()) {
        NSData.create(bytes = null, length = 0u)
    } else {
        usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
        }
    }

private class FoundationNetworkEngine(
    private val config: NetworkConfig
) : NetworkEngine {
    override suspend fun execute(request: NetworkRequest): NetworkResponse =
        suspendCancellableCoroutine { continuation ->
            val nativeUrl = NSURL.URLWithString(request.url)
            if (nativeUrl == null) {
                continuation.resumeWithException(NetworkInvalidUrlException(request.url))
                return@suspendCancellableCoroutine
            }
            val nativeRequest = NSMutableURLRequest(
                uRL = nativeUrl,
                cachePolicy = request.cachePolicy.toNativeCachePolicy(),
                timeoutInterval = (request.timeoutMillis ?: config.timeoutMillis) / 1_000.0
            ).apply {
                setHTTPMethod(request.method.name)
                request.headers.forEach { (name, value) ->
                    setValue(value, forHTTPHeaderField = name)
                }
                setHTTPBody(request.body?.content?.toNSData())
            }
            val task = NSURLSession.sharedSession.dataTaskWithRequest(
                request = nativeRequest,
                completionHandler = { data: NSData?, response: NSURLResponse?, error: NSError? ->
                    if (!continuation.isActive) {
                        return@dataTaskWithRequest
                    }
                    when {
                        error != null &&
                            request.cachePolicy == NetworkCachePolicy.CACHE_ONLY ->
                            continuation.resumeWithException(
                                NetworkCacheMissException(request.url)
                            )

                        error?.code == NSURLErrorTimedOut -> continuation.resumeWithException(
                            NetworkTimeoutException(
                                requestUrl = request.url,
                                timeoutMillis = request.timeoutMillis ?: config.timeoutMillis
                            )
                        )

                        error != null -> continuation.resumeWithException(
                            NetworkTransportException(
                                requestUrl = request.url,
                                detail = error.localizedDescription,
                                cause = null
                            )
                        )

                        response !is NSHTTPURLResponse -> continuation.resumeWithException(
                            NetworkTransportException(
                                requestUrl = request.url,
                                detail = "Foundation returned a non-HTTP response"
                            )
                        )

                        else -> continuation.resume(
                            NetworkResponse(
                                request = request,
                                statusCode = response.statusCode.toInt(),
                                headers = response.allHeaderFields.toNetworkHeaders(),
                                body = data?.toByteArray() ?: ByteArray(0),
                                source = request.cachePolicy.toResponseSource()
                            )
                        )
                    }
                }
            )
            continuation.invokeOnCancellation { task.cancel() }
            task.resume()
        }

    private fun NetworkCachePolicy.toResponseSource(): NetworkResponseSource =
        when (this) {
            NetworkCachePolicy.CACHE_ONLY -> NetworkResponseSource.CACHE
            NetworkCachePolicy.NETWORK_ONLY,
            NetworkCachePolicy.NO_STORE -> NetworkResponseSource.NETWORK
            NetworkCachePolicy.DEFAULT -> NetworkResponseSource.UNKNOWN
        }

    private fun NetworkCachePolicy.toNativeCachePolicy(): ULong =
        when (this) {
            NetworkCachePolicy.DEFAULT -> NSURLRequestUseProtocolCachePolicy
            NetworkCachePolicy.NETWORK_ONLY,
            NetworkCachePolicy.NO_STORE -> NSURLRequestReloadIgnoringLocalCacheData
            NetworkCachePolicy.CACHE_ONLY -> NSURLRequestReturnCacheDataDontLoad
        }

    private fun Map<Any?, *>.toNetworkHeaders(): Map<String, List<String>> =
        entries.mapNotNull { (name, value) ->
            val headerName = name?.toString()?.takeIf(String::isNotBlank)
            val headerValue = value?.toString()
            if (headerName == null || headerValue == null) null else headerName to headerValue
        }.groupBy(
            keySelector = Pair<String, String>::first,
            valueTransform = Pair<String, String>::second
        )

    private fun NSData.toByteArray(): ByteArray {
        if (length == 0uL) return ByteArray(0)
        return ByteArray(length.toInt()).also { result ->
            result.usePinned { pinned ->
                memcpy(pinned.addressOf(0), bytes, length)
            }
        }
    }
}
