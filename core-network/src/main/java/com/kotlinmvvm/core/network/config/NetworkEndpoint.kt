package com.kotlinmvvm.core.network.config

import java.net.URI
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 单个远程服务的地址配置，使同一应用可安全注册和复用多个 Base URL
 */
data class NetworkEndpoint(
    val name: String,
    val baseUrl: String,
    val allowCleartext: Boolean = false
) {
    init {
        val uri = runCatching { URI(baseUrl) }.getOrNull()
        validate(name.isNotBlank(), NetworkConfigurationError.BLANK_ENDPOINT_NAME)
        validate(uri?.host?.isNotBlank() == true, NetworkConfigurationError.INVALID_BASE_URL_HOST)
        validate(baseUrl.endsWith('/'), NetworkConfigurationError.BASE_URL_MISSING_TRAILING_SLASH)
        validate(
            uri?.rawQuery == null && uri?.rawFragment == null,
            NetworkConfigurationError.BASE_URL_HAS_QUERY_OR_FRAGMENT
        )
        validate(
            uri?.scheme == HTTPS_SCHEME || allowCleartext && uri?.scheme == HTTP_SCHEME,
            NetworkConfigurationError.HTTPS_REQUIRED
        )
    }

    /** 判断请求地址是否属于当前 Endpoint 的同协议、同主机和同端口边界。 */
    fun permits(url: HttpUrl): Boolean = parsedBaseUrl.let { base ->
        url.scheme == base.scheme && url.host == base.host && url.port == base.port
    }

    private val parsedBaseUrl: HttpUrl by lazy(LazyThreadSafetyMode.PUBLICATION) {
        baseUrl.toHttpUrl()
    }

    private fun validate(isValid: Boolean, error: NetworkConfigurationError) {
        if (!isValid) throw NetworkConfigurationException(error)
    }

    private companion object {
        const val HTTPS_SCHEME = "https"
        const val HTTP_SCHEME = "http"
    }
}
