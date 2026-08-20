package com.kotlinmvvm.core.network.interceptor

import com.kotlinmvvm.core.network.config.NetworkEndpoint
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 网络请求主机白名单拦截器，阻止分页地址和重定向逃逸已注册 Endpoint
 */
class NetworkEndpointAllowlistInterceptor(
    private val endpoints: () -> Collection<NetworkEndpoint>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (endpoints().none { it.permits(request.url) }) {
            throw UnregisteredNetworkEndpointException(request.url.host)
        }
        return chain.proceed(request)
    }
}
