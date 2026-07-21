package com.kotlinmvvm.core.network.result

/**
 * @author 浩楠
 * @date 2026/7/21 14:17
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: HTTP 或底层传输协议不兼容失败类型，默认不重试以避免无效请求风暴
 */
data object NetworkProtocolFailure : NetworkFailure {
    override val diagnosticCode = "protocol"
    override val retryable = false
}
