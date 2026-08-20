package com.kotlinmvvm.core.network.stream

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 表示远端已经发起 WebSocket 关闭握手
 */
data class NetworkWebSocketClosing(
    val code: Int,
    val reason: String
) : NetworkWebSocketEvent
