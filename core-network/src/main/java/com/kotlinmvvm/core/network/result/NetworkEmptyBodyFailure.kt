package com.kotlinmvvm.core.network.result

/**
 * @author 浩楠
 * @date 2026/7/21 14:17
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 成功响应正文缺失类型，表示接口要求实体但服务端未返回有效内容
 */
data object NetworkEmptyBodyFailure : NetworkFailure {
    override val diagnosticCode = "empty_body"
    override val retryable = false
}
