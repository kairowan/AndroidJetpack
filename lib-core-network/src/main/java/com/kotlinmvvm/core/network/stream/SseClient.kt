package com.kotlinmvvm.core.network.stream

import com.kotlinmvvm.core.network.config.NetworkEndpoint
import com.kotlinmvvm.core.network.exception.NetworkExceptionHandler
import com.kotlinmvvm.core.network.result.NetworkHttpFailure
import com.kotlinmvvm.core.network.result.NetworkProtocolFailure
import java.util.concurrent.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 负责建立和消费单次 SSE 连接，并在 Flow 取消时释放底层网络调用
 */
class SseClient internal constructor(
    private val client: OkHttpClient,
    private val endpoint: NetworkEndpoint,
    private val exceptionHandler: NetworkExceptionHandler = NetworkExceptionHandler()
) {
    /**
     * 返回服务端事件流；业务层负责根据事件编号、幂等语义和退避策略决定是否重连。
     */
    fun events(
        relativeUrl: String,
        headers: Map<String, String> = emptyMap(),
        lastEventId: String? = null
    ): Flow<SseEvent> = callbackFlow {
        val request = endpoint.request(relativeUrl, headers)
            .newBuilder()
            .header(HEADER_ACCEPT, MIME_EVENT_STREAM)
            .header(HEADER_CACHE_CONTROL, CACHE_NO_CACHE)
            .apply {
                lastEventId?.let { header(HEADER_LAST_EVENT_ID, it) }
            }
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                if (!call.isCanceled()) {
                    close(NetworkStreamException(exceptionHandler.handleException(e), e))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        if (!response.isSuccessful) {
                            throw NetworkStreamException(NetworkHttpFailure(response.code))
                        }
                        if (response.code == HTTP_NO_CONTENT) {
                            close()
                            return
                        }
                        if (response.body.contentType()?.let {
                                it.type == MIME_TEXT && it.subtype == MIME_EVENT_STREAM_SUBTYPE
                            } != true
                        ) {
                            throw NetworkStreamException(NetworkProtocolFailure)
                        }
                        SseParser(lastEventId) { event ->
                            trySendBlocking(event).getOrThrow()
                        }.parse(response.body.source())
                        close()
                    } catch (_: CancellationException) {
                        close()
                    } catch (error: NetworkStreamException) {
                        close(error)
                    } catch (error: Exception) {
                        if (!call.isCanceled()) {
                            close(
                                NetworkStreamException(
                                    exceptionHandler.handleException(error),
                                    error
                                )
                            )
                        }
                    }
                }
            }
        })
        awaitClose(call::cancel)
    }

    private companion object {
        const val HEADER_ACCEPT = "Accept"
        const val HEADER_CACHE_CONTROL = "Cache-Control"
        const val HEADER_LAST_EVENT_ID = "Last-Event-ID"
        const val MIME_EVENT_STREAM = "text/event-stream"
        const val MIME_TEXT = "text"
        const val MIME_EVENT_STREAM_SUBTYPE = "event-stream"
        const val CACHE_NO_CACHE = "no-cache"
        const val HTTP_NO_CONTENT = 204
    }
}
