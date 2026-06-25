package com.ghn.lib.upload

import android.content.Context

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
 * 描述: 上传库对外配置与入口定义，负责封装宿主侧初始化能力。
 */

/**
 * 上传库宿主配置模型。
 */
data class UploadLibraryConfig(
    val maxConcurrentUploads: Int = 2,
    val connectTimeoutMillis: Long = 15_000L,
    val writeTimeoutMillis: Long = 30_000L,
    val readTimeoutMillis: Long = 30_000L,
    val progressThrottleMillis: Long = 200L,
    val retryBackoffMillis: Long = 10_000L,
    val maxResponseBodyChars: Int = 2_048,
)

/**
 * 上传库对外入口。
 */
object UploadLibrary {
    @Volatile
    private var initialized = false

    fun configure(config: UploadLibraryConfig = UploadLibraryConfig()) {
        synchronized(this) {
            // 只提前记住配置，避免在冷启动阶段立刻构造网络上传客户端。
            FlowUpload.configure(config = config.toFlowUploadConfig())
        }
    }

    fun warmUp(context: Context) {
        if (initialized) {
            return
        }
        synchronized(this) {
            if (initialized) {
                return
            }
            // 预热阶段通过惰性访问完成 client 创建，把初始化成本移出首屏关键路径。
            FlowUpload.get(context.applicationContext)
            initialized = true
        }
    }

    fun initialize(
        context: Context,
        config: UploadLibraryConfig = UploadLibraryConfig(),
    ) {
        configure(config)
        warmUp(context)
    }

    fun reset() {
        synchronized(this) {
            FlowUpload.reset()
            initialized = false
        }
    }
}

private fun UploadLibraryConfig.toFlowUploadConfig(): FlowUploadConfig {
    return FlowUploadConfig(
        maxConcurrentUploads = maxConcurrentUploads,
        connectTimeoutMillis = connectTimeoutMillis,
        writeTimeoutMillis = writeTimeoutMillis,
        readTimeoutMillis = readTimeoutMillis,
        progressThrottleMillis = progressThrottleMillis,
        retryBackoffMillis = retryBackoffMillis,
        maxResponseBodyChars = maxResponseBodyChars
    )
}
