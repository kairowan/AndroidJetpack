package com.kotlinmvvm.core.network.config

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 不可信远程资源地址策略，只允许显式 Host 与 HTTPS 端口并可升级受控旧式 HTTP 地址
 */
class RemoteResourceUrlPolicy(
    allowedHosts: Set<String>,
    upgradeCleartextHosts: Set<String> = emptySet(),
    allowedHttpsPorts: Set<Int> = setOf(DEFAULT_HTTPS_PORT)
) {
    private val allowedHosts = allowedHosts.mapTo(mutableSetOf(), ::normalizeConfiguredHost)
    private val upgradeCleartextHosts =
        upgradeCleartextHosts.mapTo(mutableSetOf(), ::normalizeConfiguredHost)
    private val allowedHttpsPorts = allowedHttpsPorts.toSet()

    init {
        validate(this.allowedHosts.isNotEmpty(), NetworkConfigurationError.EMPTY_RESOURCE_HOSTS)
        validate(
            this.upgradeCleartextHosts.all(this.allowedHosts::contains),
            NetworkConfigurationError.RESOURCE_UPGRADE_HOST_NOT_ALLOWED
        )
        validate(
            allowedHttpsPorts.isNotEmpty() && allowedHttpsPorts.all { it in VALID_PORTS },
            NetworkConfigurationError.INVALID_RESOURCE_HTTPS_PORT
        )
    }

    /**
     * 校验并规范化服务端返回的资源地址；不可信 Host、用户信息、明文或未授权端口返回 null。
     */
    fun normalize(rawUrl: String?): String? {
        val url = rawUrl?.trim()?.toHttpUrlOrNull() ?: return null
        if (url.username.isNotEmpty() || url.password.isNotEmpty() || url.host !in allowedHosts) {
            return null
        }
        return when {
            url.isHttps && url.port in allowedHttpsPorts -> url.newBuilder().fragment(null).build().toString()
            url.scheme == HTTP_SCHEME && url.port == DEFAULT_HTTP_PORT &&
                url.host in upgradeCleartextHosts && DEFAULT_HTTPS_PORT in allowedHttpsPorts ->
                url.newBuilder()
                    .scheme(HTTPS_SCHEME)
                    .port(DEFAULT_HTTPS_PORT)
                    .fragment(null)
                    .build()
                    .toString()

            else -> null
        }
    }

    /**
     * 判断真实网络交换是否仍位于 HTTPS 白名单内；该检查不执行明文升级，适合拦截重定向。
     */
    fun permitsNetworkRequest(rawUrl: String?): Boolean {
        val url = rawUrl?.trim()?.toHttpUrlOrNull() ?: return false
        return url.username.isEmpty() &&
            url.password.isEmpty() &&
            url.isHttps &&
            url.host in allowedHosts &&
            url.port in allowedHttpsPorts
    }

    private fun validate(isValid: Boolean, error: NetworkConfigurationError) {
        if (!isValid) throw NetworkConfigurationException(error)
    }

    private companion object {
        const val HTTP_SCHEME = "http"
        const val HTTPS_SCHEME = "https"
        const val DEFAULT_HTTP_PORT = 80
        const val DEFAULT_HTTPS_PORT = 443
        val VALID_PORTS = 1..65_535

        fun normalizeConfiguredHost(value: String): String {
            val host = value.trim().lowercase().removeSuffix(".")
            if (host.isEmpty() || host.any { it in "/:@?#" }) invalidResourceHost()
            val parsed = "$HTTPS_SCHEME://$host/".toHttpUrlOrNull()
            if (parsed?.host != host) invalidResourceHost()
            return host
        }

        fun invalidResourceHost(): Nothing = throw NetworkConfigurationException(
            NetworkConfigurationError.INVALID_RESOURCE_HOST
        )
    }
}
