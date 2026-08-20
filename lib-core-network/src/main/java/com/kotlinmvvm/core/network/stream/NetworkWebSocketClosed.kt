package com.kotlinmvvm.core.network.stream

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 表示 WebSocket 关闭握手已经完成且不会再产生消息
 */
data class NetworkWebSocketClosed(
    val code: Int,
    val reason: String
) : NetworkWebSocketEvent
