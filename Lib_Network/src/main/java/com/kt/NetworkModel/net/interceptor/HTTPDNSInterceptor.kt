package com.kt.NetworkModel.net.interceptor

import android.content.Context
import com.alibaba.sdk.android.httpdns.HttpDns
import com.kt.NetworkModel.provider.IHeaderProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author 浩楠
 *
 * @date 2026-2-18
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

/**
 * 拦截请求头
 */
class HTTPDNSInterceptor(private val context: Context,private val headerProvider: IHeaderProvider?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val builder = originRequest.newBuilder()
        headerProvider?.getHeaders()?.forEach { (key, value) ->
            builder.header(key, value)
        }
        return chain.proceed(builder.build())
    }
}