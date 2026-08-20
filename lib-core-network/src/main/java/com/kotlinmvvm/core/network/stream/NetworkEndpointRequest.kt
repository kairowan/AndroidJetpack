package com.kotlinmvvm.core.network.stream

import com.kotlinmvvm.core.network.config.NetworkEndpoint
import com.kotlinmvvm.core.network.interceptor.UnregisteredNetworkEndpointException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

/** 在创建长连接前解析相对地址，并阻止请求逃逸到未注册的服务端点。 */
internal fun NetworkEndpoint.request(
    relativeUrl: String,
    headers: Map<String, String>
): Request {
    require(relativeUrl.isNotBlank()) { "relativeUrl must not be blank" }
    val url: HttpUrl = baseUrl.toHttpUrl().resolve(relativeUrl)
        ?: throw IllegalArgumentException("invalid relativeUrl")
    if (!permits(url)) throw UnregisteredNetworkEndpointException(url.host)
    return Request.Builder()
        .url(url)
        .apply { headers.forEach(::header) }
        .build()
}
