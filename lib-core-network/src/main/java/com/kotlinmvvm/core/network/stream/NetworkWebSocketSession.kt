package com.kotlinmvvm.core.network.stream

import com.kotlinmvvm.core.network.exception.NetworkExceptionHandler
import com.kotlinmvvm.core.network.result.NetworkHttpFailure
import com.kotlinmvvm.core.network.result.NetworkUnexpectedFailure
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onCompletion
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 管理单个 WebSocket 的消息收发、关闭握手和单消费者事件生命周期
 */
class NetworkWebSocketSession internal constructor(
    client: OkHttpClient,
    request: Request,
    private val exceptionHandler: NetworkExceptionHandler
) {
    private val eventChannel = Channel<NetworkWebSocketEvent>(Channel.BUFFERED)
    private val webSocket: WebSocket = client.newWebSocket(request, Listener())

    /** 事件只允许一个 owner 收集，收集结束会立即取消底层连接。 */
    val events: Flow<NetworkWebSocketEvent> = eventChannel
        .consumeAsFlow()
        .onCompletion { this@NetworkWebSocketSession.cancel() }

    /** 尝试发送文本；返回 false 表示会话关闭中或发送队列已达到限制。 */
    fun send(text: String): Boolean = webSocket.send(text)

    /** 尝试发送二进制内容；返回 false 表示会话关闭中或发送队列已达到限制。 */
    fun send(bytes: ByteArray): Boolean = webSocket.send(bytes.toByteString())

    /** 返回等待发送的字节数，供业务层实施自己的背压策略。 */
    fun queuedBytes(): Long = webSocket.queueSize()

    /** 发起标准关闭握手并等待远端确认。 */
    fun close(code: Int = NORMAL_CLOSE_CODE, reason: String? = null): Boolean =
        webSocket.close(code, reason)

    /** 立即取消连接并停止事件流，不执行关闭握手。 */
    fun cancel() {
        webSocket.cancel()
        eventChannel.close()
    }

    private inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            publish(NetworkWebSocketOpen)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            publish(NetworkWebSocketText(text))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            publish(NetworkWebSocketBinary(bytes.toByteArray()))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            publish(NetworkWebSocketClosing(code, reason))
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            publish(NetworkWebSocketClosed(code, reason))
            eventChannel.close()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            val failure = response
                ?.takeIf { it.code != SWITCHING_PROTOCOLS_CODE }
                ?.let { NetworkHttpFailure(it.code) }
                ?: (t as? Exception)?.let(exceptionHandler::handleException)
                ?: NetworkUnexpectedFailure
            publish(NetworkWebSocketFailure(failure))
            eventChannel.close()
        }
    }

    private fun publish(event: NetworkWebSocketEvent) {
        eventChannel.trySendBlocking(event)
    }

    private companion object {
        const val NORMAL_CLOSE_CODE = 1000
        const val SWITCHING_PROTOCOLS_CODE = 101
    }
}
