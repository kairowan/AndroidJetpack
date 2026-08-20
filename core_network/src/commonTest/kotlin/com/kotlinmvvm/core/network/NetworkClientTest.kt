package com.kotlinmvvm.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class NetworkClientTest {
    @Test
    fun appliesDefaultsAndInterceptorsBeforeExecuting() = runBlocking {
        var executedRequest: NetworkRequest? = null
        val engine = NetworkEngine { request ->
            executedRequest = request
            request.successResponse("ok")
        }
        val client = NetworkClient(
            engine = engine,
            config = NetworkConfig(
                baseUrl = "https://example.com/api",
                defaultHeaders = mapOf("Accept" to "application/json")
            ),
            interceptors = listOf(
                NetworkInterceptor { chain ->
                    chain.proceed(
                        chain.request.copy(
                            headers = chain.request.headers + ("Authorization" to "Bearer token")
                        )
                    )
                }
            )
        )

        assertEquals(
            "ok",
            client.getText(
                url = "/feed",
                queryParameters = mapOf("keyword" to "KMP & CMP")
            )
        )
        assertEquals(
            "https://example.com/api/feed?keyword=KMP%20%26%20CMP",
            executedRequest?.url
        )
        assertEquals(emptyMap(), executedRequest?.queryParameters)
        assertEquals("application/json", executedRequest?.headers?.get("Accept"))
        assertEquals("Bearer token", executedRequest?.headers?.get("Authorization"))
    }

    @Test
    fun requestHeadersOverrideDefaultsCaseInsensitivelyAndNoStoreIsEnforced() = runBlocking {
        var executedRequest: NetworkRequest? = null
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                executedRequest = request
                request.successResponse("ok")
            },
            config = NetworkConfig(
                defaultHeaders = mapOf(
                    "accept" to "text/plain",
                    "Cache-Control" to "max-age=3600"
                )
            )
        )

        client.get(
            url = "https://example.com/profile",
            headers = mapOf("Accept" to "application/json"),
            cachePolicy = NetworkCachePolicy.NO_STORE
        )

        assertEquals(
            mapOf(
                "Accept" to "application/json",
                "Cache-Control" to "no-store"
            ),
            executedRequest?.headers
        )
    }

    @Test
    fun appendsEncodedQueryBeforeFragmentAndKeepsExistingQuery() = runBlocking {
        var executedRequest: NetworkRequest? = null
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                executedRequest = request
                request.successResponse("ok")
            }
        )

        client.execute(
            NetworkRequest(
                url = "https://example.com/feed?sort=new#top",
                queryParameters = linkedMapOf(
                    "page" to "2",
                    "title" to "跨端"
                )
            )
        )

        assertEquals(
            "https://example.com/feed?sort=new&page=2&title=%E8%B7%A8%E7%AB%AF#top",
            executedRequest?.url
        )
    }

    @Test
    fun preparesJsonAndFormBodiesForBothPlatformEngines() = runBlocking {
        val executedRequests = mutableListOf<NetworkRequest>()
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                executedRequests += request
                request.successResponse("ok")
            }
        )

        client.send(
            url = "https://example.com/profile",
            method = NetworkMethod.POST,
            body = NetworkBody.json("""{"nickname":"CMP"}""")
        )
        client.send(
            url = "https://example.com/search",
            method = NetworkMethod.POST,
            body = NetworkBody.form(
                linkedMapOf(
                    "keyword" to "KMP & CMP",
                    "page" to "1"
                )
            )
        )

        assertEquals(
            "application/json; charset=utf-8",
            executedRequests[0].headers["Content-Type"]
        )
        assertEquals(
            """{"nickname":"CMP"}""",
            executedRequests[0].body?.content?.decodeToString()
        )
        assertEquals(
            "application/x-www-form-urlencoded; charset=utf-8",
            executedRequests[1].headers["Content-Type"]
        )
        assertEquals(
            "keyword=KMP+%26+CMP&page=1",
            executedRequests[1].body?.content?.decodeToString()
        )
    }

    @Test
    fun rejectsInvalidHeadersBeforeTheyReachAPlatformEngine() {
        assertFailsWith<IllegalArgumentException> {
            NetworkRequest(
                url = "https://example.com",
                headers = mapOf("Bad Header" to "value")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            NetworkRequest(
                url = "https://example.com",
                headers = mapOf("客户端" to "value")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            NetworkRequest(
                url = "https://example.com",
                headers = mapOf("X-Value" to "safe\r\nInjected: value")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            NetworkRequest(
                url = "https://example.com",
                headers = mapOf("X-Value" to "unsafe\u0000value")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            NetworkRequest(
                url = "https://example.com",
                headers = mapOf("X-Value" to "中文")
            )
        }
        assertFailsWith<IllegalArgumentException> {
            NetworkBody.binary(
                content = byteArrayOf(1),
                contentType = "image/png\r\nInjected: value"
            )
        }
    }

    @Test
    fun cacheFirstUsesCacheThenFallsBackToNetworkOnAMiss() = runBlocking {
        val attempts = mutableListOf<NetworkCachePolicy>()
        var cached = true
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                attempts += request.cachePolicy
                if (
                    request.cachePolicy == NetworkCachePolicy.CACHE_ONLY &&
                    !cached
                ) {
                    throw NetworkCacheMissException(request.url)
                }
                request.successResponse(
                    body = if (request.cachePolicy == NetworkCachePolicy.CACHE_ONLY) {
                        "cached"
                    } else {
                        "network"
                    },
                    source = if (request.cachePolicy == NetworkCachePolicy.CACHE_ONLY) {
                        NetworkResponseSource.CACHE
                    } else {
                        NetworkResponseSource.NETWORK
                    }
                )
            }
        )

        val cacheHit = client.getCacheFirst("https://example.com/feed")
        cached = false
        val networkFallback = client.getCacheFirst("https://example.com/feed")

        assertEquals("cached", cacheHit.bodyText())
        assertEquals(NetworkResponseSource.CACHE, cacheHit.source)
        assertEquals("network", networkFallback.bodyText())
        assertEquals(NetworkResponseSource.NETWORK, networkFallback.source)
        assertEquals(
            listOf(
                NetworkCachePolicy.CACHE_ONLY,
                NetworkCachePolicy.CACHE_ONLY,
                NetworkCachePolicy.NETWORK_ONLY
            ),
            attempts
        )
    }

    @Test
    fun networkFirstFallsBackOnlyForRecoverableFailures() = runBlocking {
        var responseCode = 503
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                when (request.cachePolicy) {
                    NetworkCachePolicy.NETWORK_ONLY -> NetworkResponse(
                        request = request,
                        statusCode = responseCode,
                        headers = emptyMap(),
                        body = ByteArray(0),
                        source = NetworkResponseSource.NETWORK
                    )

                    NetworkCachePolicy.CACHE_ONLY -> request.successResponse(
                        body = "cached",
                        source = NetworkResponseSource.CACHE
                    )

                    else -> error("unexpected cache policy")
                }
            }
        )

        assertEquals(
            "cached",
            client.getNetworkFirst("https://example.com/feed").bodyText()
        )
        responseCode = 404
        assertFailsWith<NetworkHttpException> {
            client.getNetworkFirst("https://example.com/feed")
        }
        Unit
    }

    @Test
    fun networkFirstPreservesTheNetworkFailureWhenCacheIsMissing() = runBlocking {
        val networkFailure = NetworkTransportException(
            requestUrl = "https://example.com/feed",
            detail = "offline"
        )
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                if (request.cachePolicy == NetworkCachePolicy.NETWORK_ONLY) {
                    throw networkFailure
                }
                throw NetworkCacheMissException(request.url)
            }
        )

        val thrown = assertFailsWith<NetworkTransportException> {
            client.getNetworkFirst("https://example.com/feed")
        }

        assertEquals(networkFailure, thrown)
    }

    @Test
    fun retriesOnlyConfiguredIdempotentFailures() = runBlocking {
        var attempts = 0
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                attempts += 1
                if (attempts < 3) {
                    throw NetworkTransportException(request.url, "offline")
                }
                request.successResponse("done")
            },
            config = NetworkConfig(
                retryPolicy = NetworkRetryPolicy(
                    maxRetries = 2,
                    initialDelayMillis = 0,
                    maxDelayMillis = 0
                )
            )
        )

        assertEquals("done", client.getText("https://example.com/feed"))
        assertEquals(3, attempts)
    }

    @Test
    fun mapsHttpErrorsWithoutRetryingPostByDefault() = runBlocking {
        var attempts = 0
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                attempts += 1
                NetworkResponse(
                    request = request,
                    statusCode = 503,
                    headers = emptyMap(),
                    body = ByteArray(0)
                )
            },
            config = NetworkConfig(
                retryPolicy = NetworkRetryPolicy(
                    maxRetries = 2,
                    initialDelayMillis = 0,
                    maxDelayMillis = 0
                )
            )
        )

        val error = assertFailsWith<NetworkHttpException> {
            client.execute(
                NetworkRequest(
                    url = "https://example.com/feed",
                    method = NetworkMethod.POST
                )
            )
        }
        assertEquals(503, error.response.statusCode)
        assertEquals(1, attempts)
    }

    @Test
    fun retriesAnExplicitlyConfiguredPost() = runBlocking {
        var attempts = 0
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                attempts += 1
                if (attempts == 1) {
                    throw NetworkTransportException(request.url, "offline")
                }
                request.successResponse("done")
            },
            config = NetworkConfig(
                retryPolicy = NetworkRetryPolicy(
                    maxRetries = 1,
                    initialDelayMillis = 0,
                    maxDelayMillis = 0,
                    retryMethods = setOf(NetworkMethod.POST)
                )
            )
        )

        val response = client.send(
            url = "https://example.com/feed",
            method = NetworkMethod.POST
        )

        assertEquals("done", response.bodyText())
        assertEquals(2, attempts)
    }

    @Test
    fun appliesRetryAfterWithoutWaitingPastTheConfiguredLimit() {
        val policy = NetworkRetryPolicy(maxRetryAfterMillis = 5_000)

        assertEquals(
            3_000,
            policy.retryDelayMillisFor(
                failure = httpFailure(retryAfter = "3"),
                fallbackDelayMillis = 250
            )
        )
        assertEquals(
            500,
            policy.retryDelayMillisFor(
                failure = httpFailure(retryAfter = "Wed, 21 Oct 2015 07:28:00 GMT"),
                fallbackDelayMillis = 500
            )
        )
        assertEquals(
            null,
            policy.retryDelayMillisFor(
                failure = httpFailure(retryAfter = "6"),
                fallbackDelayMillis = 250
            )
        )
        assertEquals(
            null,
            policy.retryDelayMillisFor(
                failure = httpFailure(retryAfter = "999999999999999999999999"),
                fallbackDelayMillis = 250
            )
        )
    }

    @Test
    fun rejectsANegativeRetryAfterLimit() {
        assertFailsWith<IllegalArgumentException> {
            NetworkRetryPolicy(maxRetryAfterMillis = -1)
        }
    }

    @Test
    fun preservesCoroutineCancellation() = runBlocking {
        val cancellation = CancellationException("cancelled by caller")
        val client = NetworkClient(
            engine = NetworkEngine { throw cancellation }
        )

        val thrown = assertFailsWith<CancellationException> {
            client.get("https://example.com/feed")
        }
        assertIs<CancellationException>(thrown)
        assertEquals(cancellation.message, thrown.message)
    }

    @Test
    fun distinguishesRequestTimeoutFromCallerTimeout() = runBlocking {
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                delay(500)
                request.successResponse("late")
            },
            config = NetworkConfig(timeoutMillis = 20)
        )

        val requestTimeout = assertFailsWith<NetworkTimeoutException> {
            client.get("https://example.com/feed")
        }
        assertEquals(20, requestTimeout.timeoutMillis)

        assertFailsWith<TimeoutCancellationException> {
            withTimeout(5) {
                client.get(
                    url = "https://example.com/feed",
                    timeoutMillis = 500
                )
            }
        }
        Unit
    }

    @Test
    fun rejectsCleartextBeforeCallingThePlatformEngine() = runBlocking {
        var engineCalled = false
        val client = NetworkClient(
            engine = NetworkEngine { request ->
                engineCalled = true
                request.successResponse("unexpected")
            }
        )

        assertFailsWith<NetworkInvalidUrlException> {
            client.get("http://example.com/feed")
        }
        assertEquals(false, engineCalled)
    }

    private fun NetworkRequest.successResponse(
        body: String,
        source: NetworkResponseSource = NetworkResponseSource.UNKNOWN
    ): NetworkResponse =
        NetworkResponse(
            request = this,
            statusCode = 200,
            headers = emptyMap(),
            body = body.encodeToByteArray(),
            source = source
        )

    private fun httpFailure(retryAfter: String): NetworkHttpException {
        val request = NetworkRequest("https://example.com/feed")
        return NetworkHttpException(
            NetworkResponse(
                request = request,
                statusCode = 429,
                headers = mapOf("Retry-After" to listOf(retryAfter)),
                body = ByteArray(0)
            )
        )
    }
}
