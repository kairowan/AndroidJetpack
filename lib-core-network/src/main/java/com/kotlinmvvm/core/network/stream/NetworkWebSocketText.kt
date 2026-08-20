package com.kotlinmvvm.core.network.stream

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 表示 WebSocket 接收到的一条文本消息
 */
data class NetworkWebSocketText(val value: String) : NetworkWebSocketEvent
