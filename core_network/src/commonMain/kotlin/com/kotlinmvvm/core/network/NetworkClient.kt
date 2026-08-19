package com.kotlinmvvm.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeout

fun interface NetworkInterceptor {
    suspend fun intercept(chain: NetworkInterceptorChain): NetworkResponse
}

interface NetworkInterceptorChain {
    val request: NetworkRequest

    suspend fun proceed(request: NetworkRequest = this.request): NetworkResponse
}

class NetworkClient internal constructor(
    private val engine: NetworkEngine,
    private val config: NetworkConfig = NetworkConfig(),
    private val interceptors: List<NetworkInterceptor> = emptyList()
) {
    suspend fun execute(request: NetworkRequest): NetworkResponse =
        executeWithRetry(request.prepare())

    suspend fun send(
        url: String,
        method: NetworkMethod,
        body: NetworkBody? = null,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        timeoutMillis: Long? = null,
        cachePolicy: NetworkCachePolicy = NetworkCachePolicy.DEFAULT
    ): NetworkResponse = execute(
        NetworkRequest(
            url = url,
            method = method,
            queryParameters = queryParameters,
            headers = headers,
            body = body,
            timeoutMillis = timeoutMillis,
            cachePolicy = cachePolicy
        )
    )

    suspend fun get(
        url: String,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        timeoutMillis: Long? = null,
        cachePolicy: NetworkCachePolicy = NetworkCachePolicy.DEFAULT
    ): NetworkResponse = send(
        url = url,
        method = NetworkMethod.GET,
        queryParameters = queryParameters,
        headers = headers,
        timeoutMillis = timeoutMillis,
        cachePolicy = cachePolicy
    )

    suspend fun getCacheFirst(
        url: String,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        timeoutMillis: Long? = null
    ): NetworkResponse {
        val request = NetworkRequest(
            url = url,
            queryParameters = queryParameters,
            headers = headers,
            timeoutMillis = timeoutMillis
        ).prepare()
        return try {
            executeWithRetry(request.copy(cachePolicy = NetworkCachePolicy.CACHE_ONLY))
        } catch (_: NetworkCacheMissException) {
            executeWithRetry(request.copy(cachePolicy = NetworkCachePolicy.NETWORK_ONLY))
        }
    }

    suspend fun getNetworkFirst(
        url: String,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        timeoutMillis: Long? = null
    ): NetworkResponse {
        val request = NetworkRequest(
            url = url,
            queryParameters = queryParameters,
            headers = headers,
            timeoutMillis = timeoutMillis
        ).prepare()
        return try {
            executeWithRetry(request.copy(cachePolicy = NetworkCachePolicy.NETWORK_ONLY))
        } catch (networkFailure: NetworkException) {
            if (!networkFailure.allowsCacheFallback()) throw networkFailure
            try {
                executeWithRetry(request.copy(cachePolicy = NetworkCachePolicy.CACHE_ONLY))
            } catch (_: NetworkCacheMissException) {
                throw networkFailure
            }
        }
    }

    suspend fun getBytes(
        url: String,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        timeoutMillis: Long? = null,
        cachePolicy: NetworkCachePolicy = NetworkCachePolicy.DEFAULT
    ): ByteArray = get(
        url = url,
        queryParameters = queryParameters,
        headers = headers,
        timeoutMillis = timeoutMillis,
        cachePolicy = cachePolicy
    ).body

    suspend fun getText(
        url: String,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        timeoutMillis: Long? = null,
        cachePolicy: NetworkCachePolicy = NetworkCachePolicy.DEFAULT
    ): String = get(
        url = url,
        queryParameters = queryParameters,
        headers = headers,
        timeoutMillis = timeoutMillis,
        cachePolicy = cachePolicy
    ).bodyText()

    private suspend fun executeWithRetry(request: NetworkRequest): NetworkResponse {
        var retryCount = 0
        var retryDelayMillis = config.retryPolicy.initialDelayMillis

        while (true) {
            val failure = try {
                val response = withTimeout(request.timeoutMillis ?: config.timeoutMillis) {
                    RealInterceptorChain(interceptors, 0, request, engine).proceed()
                }
                if (response.isSuccessful) return response
                NetworkHttpException(response)
            } catch (error: TimeoutCancellationException) {
                currentCoroutineContext().ensureActive()
                NetworkTimeoutException(
                    requestUrl = request.url,
                    timeoutMillis = request.timeoutMillis ?: config.timeoutMillis
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                error as? NetworkException ?: NetworkTransportException(
                    requestUrl = request.url,
                    detail = error.message ?: error::class.simpleName.orEmpty(),
                    cause = error
                )
            }

            if (!failure.shouldRetry(request, retryCount)) throw failure
            val delayMillis = config.retryPolicy.retryDelayMillisFor(
                failure = failure,
                fallbackDelayMillis = retryDelayMillis
            ) ?: throw failure
            retryCount += 1
            if (delayMillis > 0) delay(delayMillis)
            retryDelayMillis = retryDelayMillis.doubledUpTo(
                config.retryPolicy.maxDelayMillis
            )
        }
    }

    private fun NetworkException.shouldRetry(
        request: NetworkRequest,
        retryCount: Int
    ): Boolean {
        if (
            retryCount >= config.retryPolicy.maxRetries ||
            request.method !in config.retryPolicy.retryMethods
        ) {
            return false
        }
        return when (this) {
            is NetworkHttpException -> statusCodeIsRetryable()
            is NetworkTimeoutException,
            is NetworkTransportException -> true
            is NetworkInvalidUrlException,
            is NetworkCacheMissException -> false
        }
    }

    private fun NetworkException.allowsCacheFallback(): Boolean =
        when (this) {
            is NetworkHttpException ->
                response.statusCode == 408 ||
                    response.statusCode == 429 ||
                    response.statusCode in 500..599

            is NetworkTimeoutException,
            is NetworkTransportException -> true
            is NetworkInvalidUrlException,
            is NetworkCacheMissException -> false
        }

    private fun NetworkHttpException.statusCodeIsRetryable(): Boolean =
        response.statusCode in config.retryPolicy.retryStatusCodes

    private fun NetworkRequest.prepare(): NetworkRequest {
        val mergedHeaders = config.defaultHeaders
            .overriddenBy(headers)
            .withContentType(body)
        val preparedHeaders = if (cachePolicy == NetworkCachePolicy.NO_STORE) {
            mergedHeaders.overriddenBy(mapOf("Cache-Control" to "no-store"))
        } else {
            mergedHeaders
        }
        return copy(
            url = resolveUrl(url).withQueryParameters(queryParameters),
            queryParameters = emptyMap(),
            headers = preparedHeaders
        )
    }

    private fun Map<String, String>.withContentType(
        body: NetworkBody?
    ): Map<String, String> =
        if (
            body != null &&
            keys.none { it.equals("Content-Type", ignoreCase = true) }
        ) {
            this + ("Content-Type" to body.contentType)
        } else {
            this
        }

    private fun Map<String, String>.overriddenBy(
        overrides: Map<String, String>
    ): Map<String, String> =
        toMutableMap().apply {
            overrides.forEach { (name, value) ->
                keys.firstOrNull { it.equals(name, ignoreCase = true) }?.let(::remove)
                put(name, value)
            }
        }

    private fun resolveUrl(value: String): String {
        val candidate = value.trim()
        if (candidate.startsWith("//") || candidate.contains('\\')) {
            throw NetworkInvalidUrlException(value)
        }
        return when {
            candidate.startsWith("https://") -> candidate
            candidate.startsWith("http://") || candidate.contains("://") ->
                throw NetworkInvalidUrlException(value)

            else -> {
                val baseUrl = config.baseUrl?.trim()
                    ?: throw NetworkInvalidUrlException(value)
                if (!baseUrl.startsWith("https://") || baseUrl.contains('\\')) {
                    throw NetworkInvalidUrlException(baseUrl)
                }
                "${baseUrl.trimEnd('/')}/${candidate.trimStart('/')}"
            }
        }
    }

    private fun String.withQueryParameters(
        parameters: Map<String, String>
    ): String {
        if (parameters.isEmpty()) return this
        val fragmentIndex = indexOf('#')
        val requestUrl = if (fragmentIndex < 0) this else substring(0, fragmentIndex)
        val fragment = if (fragmentIndex < 0) "" else substring(fragmentIndex)
        val separator = when {
            '?' !in requestUrl -> "?"
            requestUrl.endsWith('?') || requestUrl.endsWith('&') -> ""
            else -> "&"
        }
        val encodedParameters = parameters.entries.joinToString("&") { (name, value) ->
            "${name.encodeUrlComponent()}=${value.encodeUrlComponent()}"
        }
        return requestUrl + separator + encodedParameters + fragment
    }

    private fun Long.doubledUpTo(maximum: Long): Long =
        if (this >= maximum - this) maximum else this * 2
}

internal fun NetworkRetryPolicy.retryDelayMillisFor(
    failure: NetworkException,
    fallbackDelayMillis: Long
): Long? {
    val value = (failure as? NetworkHttpException)
        ?.response
        ?.header("Retry-After")
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: return fallbackDelayMillis

    // ponytail: commonMain handles delay-seconds; add a shared clock/parser if a backend sends HTTP-date.
    if (value.any { it !in '0'..'9' }) return fallbackDelayMillis
    val seconds = value.toLongOrNull() ?: return null
    if (seconds > Long.MAX_VALUE / MILLIS_PER_SECOND) return null
    val retryAfterMillis = seconds * MILLIS_PER_SECOND
    if (retryAfterMillis > maxRetryAfterMillis) return null
    return maxOf(fallbackDelayMillis, retryAfterMillis)
}

private class RealInterceptorChain(
    private val interceptors: List<NetworkInterceptor>,
    private val index: Int,
    override val request: NetworkRequest,
    private val engine: NetworkEngine
) : NetworkInterceptorChain {
    override suspend fun proceed(request: NetworkRequest): NetworkResponse =
        interceptors.getOrNull(index)?.intercept(
            RealInterceptorChain(interceptors, index + 1, request, engine)
        ) ?: engine.execute(request)
}

private const val MILLIS_PER_SECOND = 1_000L
