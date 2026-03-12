@file:OptIn(kotlinx.cinterop.BetaInteropApi::class)

package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.data.eyepetizer.toDomainFeed
import com.kotlinmvvm.core.data.eyepetizer.toPayloadResponse
import com.kotlinmvvm.core.model.EyepetizerFeed
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLRequestReloadIgnoringLocalCacheData
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithRequest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @author 浩楠
 *
 * @date 2026-3-9
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: iOS 侧 Eyepetizer 仓库实现
 */
internal class IosEyepetizerRepository : EyepetizerRepository {
    override suspend fun getFeed(
        source: EyepetizerFeedSource,
        nextPageUrl: String?
    ): Result<EyepetizerFeed> = withContext(Dispatchers.Default) {
        runCatching {
            val request = EyepetizerRequestFactory.create(source, nextPageUrl)
            val payload = loadData(request).toPayloadResponse(request.url)
            payload.toDomainFeed()
        }
    }

    private suspend fun loadData(request: EyepetizerRequest): NSData = suspendCoroutine { continuation ->
        val requestUrl = requireNotNull(NSURL.URLWithString(request.url)) {
            throw EyepetizerInvalidUrlException(request.url)
        }
        val urlRequest = NSURLRequest(
            uRL = requestUrl,
            cachePolicy = NSURLRequestReloadIgnoringLocalCacheData,
            timeoutInterval = REQUEST_TIMEOUT_SECONDS
        )
        NSURLSession.sharedSession.dataTaskWithRequest(
            request = urlRequest,
            completionHandler = { data: NSData?, response: NSURLResponse?, error: NSError? ->
                when {
                    error != null -> continuation.resumeWithException(
                        EyepetizerRequestFailedException(
                            url = request.url,
                            detail = error.localizedDescription(),
                            cause = null
                        )
                    )

                    else -> {
                        val httpResponse = response as? NSHTTPURLResponse
                        val statusCode = httpResponse?.statusCode?.toInt()
                        if (statusCode != null && statusCode !in 200..299) {
                            continuation.resumeWithException(
                                EyepetizerHttpException(
                                    url = request.url,
                                    statusCode = statusCode,
                                    statusDescription = "Unexpected response status"
                                )
                            )
                        } else if (data != null) {
                            continuation.resume(data)
                        } else {
                            continuation.resumeWithException(
                                EyepetizerEmptyResponseException(request.url)
                            )
                        }
                    }
                }
            }
        ).resume()
    }

    private companion object {
        private const val REQUEST_TIMEOUT_SECONDS = 15.0
    }
}
