package com.ghn.lib.upload

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * @author 浩楠
 *
 * @date 2026/6/24
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 上传库统一入口，负责管理上传客户端的初始化与复用。
 */

object FlowUpload {
    @Volatile
    private var instance: FlowUploadClient? = null

    @Volatile
    private var rememberedConfig: FlowUploadConfig? = null

    @Volatile
    private var rememberedOkHttpClient: OkHttpClient? = null

    private val lock = Any()

    fun configure(
        config: FlowUploadConfig = FlowUploadConfig(),
        okHttpClient: OkHttpClient? = null,
    ) {
        synchronized(lock) {
            rememberConfigurationLocked(config, okHttpClient)
        }
    }

    fun initialize(
        context: Context,
        config: FlowUploadConfig = FlowUploadConfig(),
        okHttpClient: OkHttpClient? = null,
    ): FlowUploadClient = synchronized(lock) {
        rememberConfigurationLocked(config, okHttpClient)
        instance?.shutdown()
        return createClient(
            context.applicationContext,
            config,
            rememberedOkHttpClient
        ).also { client ->
            instance = client
        }
    }

    fun get(context: Context): FlowUploadClient {
        instance?.let { return it }
        return synchronized(lock) {
            instance ?: createClient(
                context = context.applicationContext,
                config = rememberedConfig ?: FlowUploadConfig(),
                okHttpClient = rememberedOkHttpClient
            ).also { client ->
                instance = client
            }
        }
    }

    fun reset() {
        synchronized(lock) {
            instance?.shutdown()
            instance = null
            rememberedConfig = null
            rememberedOkHttpClient = null
        }
    }

    private fun rememberConfigurationLocked(
        config: FlowUploadConfig,
        okHttpClient: OkHttpClient?,
    ) {
        rememberedConfig = config
        rememberedOkHttpClient = okHttpClient
    }

    private fun createClient(
        context: Context,
        config: FlowUploadConfig,
        okHttpClient: OkHttpClient?,
    ): FlowUploadClient {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val client = okHttpClient ?: OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutMillis, TimeUnit.MILLISECONDS)
            .writeTimeout(config.writeTimeoutMillis, TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeoutMillis, TimeUnit.MILLISECONDS)
            .build()
        return DefaultUploadRepository(
            context = context,
            okHttpClient = client,
            appScope = scope,
            config = config
        )
    }
}
