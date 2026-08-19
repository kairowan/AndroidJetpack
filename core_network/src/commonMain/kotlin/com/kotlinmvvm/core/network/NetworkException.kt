package com.kotlinmvvm.core.network

sealed class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class NetworkInvalidUrlException(
    val invalidUrl: String
) : NetworkException("Invalid network url: $invalidUrl")

class NetworkHttpException(
    val response: NetworkResponse
) : NetworkException("HTTP ${response.statusCode}: ${response.request.url}")

class NetworkTimeoutException(
    val requestUrl: String,
    val timeoutMillis: Long
) : NetworkException("Request timed out after ${timeoutMillis}ms: $requestUrl")

class NetworkCacheMissException(
    val requestUrl: String
) : NetworkException("No cached response available: $requestUrl")

class NetworkTransportException(
    val requestUrl: String,
    detail: String,
    cause: Throwable? = null
) : NetworkException("Network request failed: $detail ($requestUrl)", cause)
