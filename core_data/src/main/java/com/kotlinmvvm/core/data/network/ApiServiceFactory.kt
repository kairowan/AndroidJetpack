package com.kotlinmvvm.core.data.network

import com.kt.network.net.ApiService
import com.kt.network.net.RetrofitClient

/**
 * @author 浩楠
 *
 * @date 2026-3-1
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
object ApiServiceFactory {
    fun createApiService(hostType: Int): ApiService {
        return try {
            RetrofitClient.getInstance(null).getDefault(ApiService::class.java, hostType)
        } catch (error: Exception) {
            throw IllegalStateException(
                "RetrofitClient 未初始化，请先在 Application.onCreate 调用 RetrofitClient.getInstance(context)。",
                error
            )
        }
    }
}
