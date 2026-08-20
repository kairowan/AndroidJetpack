package com.kotlinmvvm.core.network.client

import com.kotlinmvvm.core.network.config.NetworkConfig
import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy
import com.kotlinmvvm.core.network.interceptor.RemoteResourceUrlInterceptor
import com.kotlinmvvm.core.network.interceptor.SafeRedirectInterceptor
import com.kotlinmvvm.core.network.interceptor.UntrustedRemoteResourceException
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 远程媒体 HTTP 客户端工厂，为 Coil 与 Media3 统一超时并闭合初始地址和重定向白名单
 */
class RemoteResourceHttpClientFactory(
    private val config: NetworkConfig,
    private val policy: RemoteResourceUrlPolicy
) {
    /** 创建无认证头、无正文日志且在两层拦截点校验媒体地址的独立客户端。 */
    fun create(): OkHttpClient {
        val urlGuard = RemoteResourceUrlInterceptor(policy)
        val safeRedirects = SafeRedirectInterceptor(
            isAllowed = { url -> policy.permitsNetworkRequest(url.toString()) },
            rejectedException = { url -> UntrustedRemoteResourceException(url.host) }
        )
        return OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(config.writeTimeoutSeconds, TimeUnit.SECONDS)
            .callTimeout(config.callTimeoutSeconds, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(safeRedirects)
            .addNetworkInterceptor(urlGuard)
            .build()
    }
}
