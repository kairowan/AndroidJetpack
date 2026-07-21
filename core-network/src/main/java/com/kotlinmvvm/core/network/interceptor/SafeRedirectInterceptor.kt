package com.kotlinmvvm.core.network.interceptor

import java.io.IOException
import java.net.ProtocolException
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 受控 GET/HEAD 重定向执行器，在 DNS 与建连前校验每个目标并阻止敏感头跨源传播
 */
internal class SafeRedirectInterceptor(
    private val isAllowed: (HttpUrl) -> Boolean,
    private val rejectedException: (HttpUrl) -> IOException
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        repeat(MAX_REDIRECTS + 1) { redirectCount ->
            if (!isAllowed(request.url)) throw rejectedException(request.url)
            val response = chain.proceed(request)
            val target = response.redirectTarget(request) ?: return response
            if (redirectCount == MAX_REDIRECTS) {
                response.close()
                throw ProtocolException()
            }
            response.close()
            request = request.redirectedTo(target)
        }
        throw ProtocolException()
    }

    private fun Response.redirectTarget(request: Request): HttpUrl? {
        if (code !in REDIRECT_CODES || request.method !in REDIRECTABLE_METHODS) return null
        return header(LOCATION_HEADER)?.let(request.url::resolve)
    }

    private fun Request.redirectedTo(target: HttpUrl): Request = newBuilder()
        .url(target)
        .apply {
            if (!this@redirectedTo.url.sameOrigin(target)) {
                removeHeader(AUTHORIZATION_HEADER)
                removeHeader(COOKIE_HEADER)
                removeHeader(PROXY_AUTHORIZATION_HEADER)
            }
        }
        .build()

    private fun HttpUrl.sameOrigin(other: HttpUrl): Boolean =
        scheme == other.scheme && host == other.host && port == other.port

    private companion object {
        const val MAX_REDIRECTS = 10
        const val LOCATION_HEADER = "Location"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val COOKIE_HEADER = "Cookie"
        const val PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization"
        val REDIRECT_CODES = setOf(300, 301, 302, 303, 307, 308)
        val REDIRECTABLE_METHODS = setOf("GET", "HEAD")
    }
}
