package com.kotlinmvvm.core.network.result

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: HTTP 协议失败类型，仅保留状态码并避免服务端正文和错误消息泄漏到业务层
 */
data class NetworkHttpFailure(
    val statusCode: Int
) : NetworkFailure {
    override val diagnosticCode = "http.$statusCode"
    override val retryable = statusCode == 408 || statusCode == 429 || statusCode >= 500
}
