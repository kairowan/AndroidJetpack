package com.kotlinmvvm.core.network.result

import com.kotlinmvvm.core.network.R

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 将通用网络失败映射为资源文案，供没有业务错误模型的调用方安全展示
 */
object NetworkFailureMessageResolver {

    /** 返回失败类型对应的字符串资源，不直接向界面暴露服务端正文或异常消息。 */
    fun messageResource(failure: NetworkFailure): Int = when (failure) {
        NetworkConnectionFailure -> R.string.core_network_error_connection
        NetworkTimeoutFailure -> R.string.core_network_error_timeout
        NetworkSerializationFailure -> R.string.core_network_error_serialization
        NetworkEmptyBodyFailure -> R.string.core_network_error_empty_body
        NetworkEndpointFailure -> R.string.core_network_error_unregistered_endpoint
        NetworkSecurityFailure -> R.string.core_network_error_security
        NetworkProtocolFailure -> R.string.core_network_error_protocol
        is NetworkHttpFailure -> R.string.core_network_error_http
        NetworkUnexpectedFailure -> R.string.core_network_error_unexpected
    }
}
