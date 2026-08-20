package com.kotlinmvvm.core.network.result

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 未注册服务地址失败类型，阻止请求、分页或重定向越过应用 Endpoint 白名单
 */
data object NetworkEndpointFailure : NetworkFailure {
    override val diagnosticCode = "endpoint"
    override val retryable = false
}
