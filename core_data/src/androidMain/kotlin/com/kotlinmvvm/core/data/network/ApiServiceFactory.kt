package com.kotlinmvvm.core.data.network

import com.kotlinmvvm.core.data.repository.EYEPETIZER_BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
 * @Description: Android 侧 Eyepetizer Retrofit 服务工厂
 */
internal object ApiServiceFactory {
    private val apiService: EyepetizerApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Retrofit.Builder()
            .baseUrl(EYEPETIZER_BASE_URL)
            .client(NetworkRuntime.okHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EyepetizerApiService::class.java)
    }

    fun createApiService(): EyepetizerApiService {
        return apiService
    }
}
