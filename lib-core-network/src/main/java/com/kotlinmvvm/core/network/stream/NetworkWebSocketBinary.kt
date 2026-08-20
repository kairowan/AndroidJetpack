package com.kotlinmvvm.core.network.stream

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 表示 WebSocket 接收到的一条二进制消息，并按字节内容判断相等性
 */
class NetworkWebSocketBinary(val value: ByteArray) : NetworkWebSocketEvent {
    override fun equals(other: Any?): Boolean =
        other is NetworkWebSocketBinary && value.contentEquals(other.value)

    override fun hashCode(): Int = value.contentHashCode()
}
