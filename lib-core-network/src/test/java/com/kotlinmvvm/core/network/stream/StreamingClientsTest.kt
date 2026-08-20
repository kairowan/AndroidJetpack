package com.kotlinmvvm.core.network.stream

import com.kotlinmvvm.core.network.client.NetworkClientFactory
import com.kotlinmvvm.core.network.config.NetworkConfig
import com.kotlinmvvm.core.network.config.NetworkEndpoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 验证 SSE 协议解析、请求头、停止信号以及 WebSocket 会话收发行为
 */
class StreamingClientsTest {
    @Test
    fun parsesSseFieldsWithoutLosingMultilineData() {
        val events = mutableListOf<SseEvent>()

        SseParser(initialLastEventId = "previous", emit = events::add).parse(
            Buffer().writeUtf8(
                "\uFEFF: keep-alive\n" +
                    "retry: 1500\n" +
                    "id: 7\n" +
                    "event: update\n" +
                    "data: first\n" +
                    "data: second\n\n"
            )
        )

        assertEquals(
            listOf(
                SseRetry(1500),
                SseMessage(data = "first\nsecond", id = "7", type = "update")
            ),
            events
        )
    }

    @Test
    fun streamsSseWithProtocolHeaders() = runBlocking {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(
                MockResponse.Builder()
                    .addHeader("Content-Type", "text/event-stream; charset=utf-8")
                    .body("id: 9\ndata: ready\n\n")
                    .build()
            )
            val client = factory().createSseClient(endpoint(server))

            assertEquals(
                listOf(SseMessage(data = "ready", id = "9")),
                client.events(relativeUrl = "events", lastEventId = "8").toList()
            )
            val request = server.takeRequest()
            assertEquals("text/event-stream", request.headers["Accept"])
            assertEquals("8", request.headers["Last-Event-ID"])
        }
    }

    @Test
    fun completesSseWhenServerReturnsNoContent() = runBlocking {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(MockResponse.Builder().code(204).build())

            assertTrue(
                factory()
                    .createSseClient(endpoint(server))
                    .events("events")
                    .toList()
                    .isEmpty()
            )
        }
    }

    @Test
    fun exchangesTextThroughWebSocketSession() = runBlocking {
        MockWebServer().use { server ->
            server.start()
            server.enqueue(
                MockResponse.Builder()
                    .webSocketUpgrade(object : WebSocketListener() {
                        override fun onMessage(webSocket: WebSocket, text: String) {
                            webSocket.send("echo:$text")
                            webSocket.close(1000, "done")
                        }
                    })
                    .build()
            )
            val session = factory()
                .createWebSocketClient(endpoint(server))
                .connect("socket")
            val received = async { session.events.toList() }

            assertTrue(session.send("hello"))
            val events = withTimeout(5_000) { received.await() }

            assertIs<NetworkWebSocketOpen>(events.first())
            assertTrue(events.contains(NetworkWebSocketText("echo:hello")))
            assertEquals(
                NetworkWebSocketClosed(1000, "done"),
                events.last()
            )
        }
    }

    private fun factory() = NetworkClientFactory(
        config = NetworkConfig(userAgent = "stream-test"),
        cacheDirectory = null
    )

    private fun endpoint(server: MockWebServer) = NetworkEndpoint(
        name = "test",
        baseUrl = server.url("/").toString(),
        allowCleartext = true
    )
}
