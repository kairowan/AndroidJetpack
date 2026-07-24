@file:OptIn(kotlinx.cinterop.BetaInteropApi::class)

package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.data.eyepetizer.toDomainFeedPage
import com.kotlinmvvm.core.data.eyepetizer.toPayloadResponse
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
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
 * 描述: iOS 侧 Eyepetizer 仓库实现，并保留协程取消语义
 */
internal class IosEyepetizerRepository : FeedPageRepository {
    override suspend fun loadPage(
        source: EyepetizerFeedSource,
        continuationToken: String?
    ): Result<FeedPage> = withContext(Dispatchers.Default) {
        try {
            val request = EyepetizerRequestFactory.create(
                source = source,
                nextPageUrl = continuationToken
            )
            Result.success(
                loadData(request)
                    .toPayloadResponse(request.url)
                    .toDomainFeedPage()
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun loadData(request: EyepetizerRequest): NSData =
        suspendCancellableCoroutine { continuation ->
            val requestUrl = requireNotNull(NSURL.URLWithString(request.url)) {
                throw EyepetizerInvalidUrlException(request.url)
            }
            val urlRequest = NSURLRequest(
                uRL = requestUrl,
                cachePolicy = NSURLRequestReloadIgnoringLocalCacheData,
                timeoutInterval = REQUEST_TIMEOUT_SECONDS
            )
            val task = NSURLSession.sharedSession.dataTaskWithRequest(
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
            )
            continuation.invokeOnCancellation { task.cancel() }
            task.resume()
        }

    private companion object {
        private const val REQUEST_TIMEOUT_SECONDS = 15.0
    }
}
