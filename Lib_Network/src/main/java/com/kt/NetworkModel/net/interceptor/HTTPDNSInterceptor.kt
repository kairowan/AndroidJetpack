package com.kt.NetworkModel.net.interceptor

import com.kt.NetworkModel.provider.IHeaderProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 拦截请求头
 */
class HTTPDNSInterceptor(private val headerProvider: IHeaderProvider?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val builder = originRequest.newBuilder()
        headerProvider?.getHeaders()?.forEach { (key, value) ->
            builder.header(key, value)
        }
        return chain.proceed(builder.build())
    }
}
