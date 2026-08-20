package com.kotlinmvvm.core.network.stream

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 表示服务端建议业务层采用的 SSE 重连等待时间
 */
data class SseRetry(val delayMillis: Long) : SseEvent
