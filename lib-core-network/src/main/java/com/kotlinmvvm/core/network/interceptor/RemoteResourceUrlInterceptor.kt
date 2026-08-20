package com.kotlinmvvm.core.network.interceptor

import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 图片与视频传输白名单拦截器，在认证无关的真实请求和每次重定向前拒绝越界地址
 */
class RemoteResourceUrlInterceptor(
    private val policy: RemoteResourceUrlPolicy
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!policy.permitsNetworkRequest(request.url.toString())) {
            throw UntrustedRemoteResourceException(request.url.host)
        }
        return chain.proceed(request)
    }
}
