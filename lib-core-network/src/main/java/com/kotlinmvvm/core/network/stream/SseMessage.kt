package com.kotlinmvvm.core.network.stream

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 表示服务端发送的一条 SSE 业务消息及其断点恢复标识
 */
data class SseMessage(
    val data: String,
    val id: String? = null,
    val type: String = SseEvent.DEFAULT_EVENT_TYPE
) : SseEvent
