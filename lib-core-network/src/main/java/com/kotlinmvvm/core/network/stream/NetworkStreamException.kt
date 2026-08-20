package com.kotlinmvvm.core.network.stream

import com.kotlinmvvm.core.network.result.NetworkFailure

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 封装长连接建立或消费异常，并保留网络层稳定失败分类
 */
class NetworkStreamException(
    val failure: NetworkFailure,
    cause: Throwable? = null
) : RuntimeException(failure.diagnosticCode, cause)
