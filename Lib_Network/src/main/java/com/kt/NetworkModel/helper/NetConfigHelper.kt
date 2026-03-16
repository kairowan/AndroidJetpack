package com.kt.NetworkModel.helper

import com.kt.NetworkModel.callback.INetworkCallback

/**
 * @author 浩楠
 *
 * @date 2025/12/20
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
object NetConfigHelper {
    data class NetworkConfig(
        val connectTimeoutSeconds: Long = 15L,
        val writeTimeoutSeconds: Long = 15L,
        val readTimeoutSeconds: Long = 15L,
        val cacheSizeBytes: Long = 50 * 1024 * 1024L,
        val enableCache: Boolean = true,
        val enableNoNetworkInterceptor: Boolean = true,
        val enableHeaderInterceptor: Boolean = true,
        val enableRetryInterceptor: Boolean = true,
        val retryCount: Int = 1,
        val retryIntervalMillis: Long = 300L,
        val flowTimeoutMillis: Long = 10_000L,
        val flowRetryCount: Long = 0L,
        val flowRetryIntervalMillis: Long = 0L
    )

    @Volatile
    var callback: INetworkCallback? = null
        private set

    @Volatile
    var networkConfig: NetworkConfig = NetworkConfig()
        private set

    fun init(callback: INetworkCallback, config: NetworkConfig = networkConfig) {
        this.callback = callback
        this.networkConfig = config
    }

    fun updateConfig(config: NetworkConfig) {
        this.networkConfig = config
    }
}
