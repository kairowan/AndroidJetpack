package com.ghn.lib.upload

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
 * 描述: 上传配置模型，统一描述上传客户端的运行参数。
 */

data class FlowUploadConfig(
    val maxConcurrentUploads: Int = 2,
    val connectTimeoutMillis: Long = 15_000L,
    val writeTimeoutMillis: Long = 30_000L,
    val readTimeoutMillis: Long = 30_000L,
    val progressThrottleMillis: Long = 200L,
    val retryBackoffMillis: Long = 30_000L,
    val maxResponseBodyChars: Int = 2_048,
)
