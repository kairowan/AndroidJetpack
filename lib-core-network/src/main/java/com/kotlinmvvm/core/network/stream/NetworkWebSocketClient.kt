package com.kotlinmvvm.core.network.stream

import com.kotlinmvvm.core.network.config.NetworkEndpoint
import com.kotlinmvvm.core.network.exception.NetworkExceptionHandler
import okhttp3.OkHttpClient

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 负责校验 WebSocket 请求端点并创建具有统一网络策略的会话
 */
class NetworkWebSocketClient internal constructor(
    private val client: OkHttpClient,
    private val endpoint: NetworkEndpoint,
    private val exceptionHandler: NetworkExceptionHandler = NetworkExceptionHandler()
) {
    /** 创建一个立即开始握手的 WebSocket 会话，调用方必须收集事件或主动取消。 */
    fun connect(
        relativeUrl: String,
        headers: Map<String, String> = emptyMap()
    ): NetworkWebSocketSession = NetworkWebSocketSession(
        client = client,
        request = endpoint.request(relativeUrl, headers),
        exceptionHandler = exceptionHandler
    )
}
