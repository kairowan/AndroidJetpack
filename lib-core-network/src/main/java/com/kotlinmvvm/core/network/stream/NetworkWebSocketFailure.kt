package com.kotlinmvvm.core.network.stream

import com.kotlinmvvm.core.network.result.NetworkFailure

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 表示 WebSocket 建连或传输失败后的统一网络错误分类
 */
data class NetworkWebSocketFailure(
    val failure: NetworkFailure
) : NetworkWebSocketEvent
