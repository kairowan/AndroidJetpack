package com.kotlinmvvm.core.network.stream

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 定义 SSE 连接可以向业务层发送的稳定事件契约
 */
sealed interface SseEvent {
    companion object {
        const val DEFAULT_EVENT_TYPE = "message"
    }
}
