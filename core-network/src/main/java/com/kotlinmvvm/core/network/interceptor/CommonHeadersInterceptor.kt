package com.kotlinmvvm.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

/**
 * @author 浩楠
 * @date 2026/7/20 10:38
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 为每个请求补充协议头、客户端标识和链路追踪编号，不记录敏感请求内容
 */
class CommonHeadersInterceptor(
    private val userAgent: String,
    private val headerProvider: NetworkHeaderProvider,
    private val requestIdProvider: () -> String = { UUID.randomUUID().toString() }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
            .header(HEADER_ACCEPT, MIME_JSON)
            .header(HEADER_USER_AGENT, userAgent)
            .header(HEADER_REQUEST_ID, requestIdProvider())

        headerProvider.headers(request).forEach { (name, value) ->
            if (name.isNotBlank() && value.isNotBlank()) requestBuilder.header(name, value)
        }
        return chain.proceed(requestBuilder.build())
    }

    private companion object {
        const val HEADER_ACCEPT = "Accept"
        const val HEADER_USER_AGENT = "User-Agent"
        const val HEADER_REQUEST_ID = "X-Request-ID"
        const val MIME_JSON = "application/json"
    }
}
