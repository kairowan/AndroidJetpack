package com.kt.NetworkModel.net.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 轻量级重试拦截器，仅针对瞬时网络异常进行重试。
 */
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
                    Thread.sleep(retryIntervalMillis * retryCount)
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
