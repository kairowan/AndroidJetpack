package com.kotlinmvvm.core.data.network

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

private const val DEFAULT_TIMEOUT_SECONDS: Long = 15L
private const val HTTP_CACHE_DIR: String = "eyepetizer_http_cache"
private const val HTTP_CACHE_MAX_BYTES: Long = 50L * 1024 * 1024

/**
 * @author 浩楠
 *
 * @date 2026-3-11
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: Android 网络运行时初始化入口
 */
object NetworkRuntime {
    @Volatile
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    internal fun okHttpClient(): OkHttpClient {
        val context = appContext
        return OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                if (context != null) {
                    cache(
                        Cache(
                            File(context.cacheDir, HTTP_CACHE_DIR),
                            HTTP_CACHE_MAX_BYTES
                        )
                    )
                }
            }
            .build()
    }
}
