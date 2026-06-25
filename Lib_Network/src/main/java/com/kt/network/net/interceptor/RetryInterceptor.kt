package com.kt.network.net.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class RetryInterceptor(
    private val maxRetryCount: Int,
    private val retryIntervalMillis: Long
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var retryCount = 0
        var lastError: Exception? = null

        while (retryCount <= maxRetryCount) {
            try {
                return chain.proceed(request)
            } catch (e: Exception) {
                lastError = e
                if (!canRetry(request, retryCount, e)) {
                    throw e
                }
                retryCount++
                if (retryIntervalMillis > 0) {
                    val totalSleep = retryIntervalMillis * retryCount
                    val sleepChunk = 50L
                    var slept = 0L
                    while (slept < totalSleep) {
                        if (chain.call().isCanceled()) throw InterruptedIOException("Canceled during retry backoff")
                        val step = minOf(sleepChunk, totalSleep - slept)
                        Thread.sleep(step)
                        slept += step
                    }
                }
            }
        }
        throw (lastError ?: IllegalStateException("Unknown network error"))
    }

    private fun canRetry(request: Request, currentRetryCount: Int, e: Exception): Boolean {
        if (currentRetryCount >= maxRetryCount) {
            return false
        }
        if (!request.method.equals("GET", ignoreCase = true) &&
            !request.method.equals("HEAD", ignoreCase = true)
        ) {
            return false
        }
        if (e.message?.contains("canceled", ignoreCase = true) == true) {
            return false
        }
        return e is SocketTimeoutException ||
            e is ConnectException ||
            e is UnknownHostException ||
            e is SSLException ||
            e is InterruptedIOException
    }
}
