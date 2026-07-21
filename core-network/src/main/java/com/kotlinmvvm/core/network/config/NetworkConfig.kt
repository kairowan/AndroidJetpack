package com.kotlinmvvm.core.network.config

/**
 * @author 浩楠
 * @date 2026/7/20 11:11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 进程级网络客户端配置，只管理所有服务共享的超时、缓存和客户端标识
 */
data class NetworkConfig(
    val connectTimeoutSeconds: Long = 15L,
    val readTimeoutSeconds: Long = 20L,
    val writeTimeoutSeconds: Long = 20L,
    val callTimeoutSeconds: Long = 30L,
    val cacheSizeBytes: Long = 20L * 1024L * 1024L,
    val userAgent: String
) {
    init {
        validate(connectTimeoutSeconds > 0L, NetworkConfigurationError.INVALID_CONNECT_TIMEOUT)
        validate(readTimeoutSeconds > 0L, NetworkConfigurationError.INVALID_READ_TIMEOUT)
        validate(writeTimeoutSeconds > 0L, NetworkConfigurationError.INVALID_WRITE_TIMEOUT)
        validate(callTimeoutSeconds > 0L, NetworkConfigurationError.INVALID_CALL_TIMEOUT)
        validate(cacheSizeBytes >= 0L, NetworkConfigurationError.INVALID_CACHE_SIZE)
        validate(userAgent.isNotBlank(), NetworkConfigurationError.BLANK_USER_AGENT)
    }

    private fun validate(isValid: Boolean, error: NetworkConfigurationError) {
        if (!isValid) throw NetworkConfigurationException(error)
    }
}
