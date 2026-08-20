package com.kotlinmvvm.core.network.client

import com.kotlinmvvm.core.network.config.NetworkConfig
import com.kotlinmvvm.core.network.config.NetworkEndpoint
import com.kotlinmvvm.core.network.config.NetworkConfigurationError
import com.kotlinmvvm.core.network.config.NetworkConfigurationException
import com.kotlinmvvm.core.network.interceptor.CommonHeadersInterceptor
import com.kotlinmvvm.core.network.interceptor.NetworkHeaderProvider
import com.kotlinmvvm.core.network.interceptor.NetworkEndpointAllowlistInterceptor
import com.kotlinmvvm.core.network.interceptor.SafeRedirectInterceptor
import com.kotlinmvvm.core.network.interceptor.UnregisteredNetworkEndpointException
import com.kotlinmvvm.core.network.stream.NetworkWebSocketClient
import com.kotlinmvvm.core.network.stream.SseClient
import okhttp3.Cache
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 复用进程级 OkHttp，在认证头注入前及每次网络交换前校验 Endpoint 白名单
 */
class NetworkClientFactory(
    private val config: NetworkConfig,
    private val cacheDirectory: File?,
    private val headerProvider: NetworkHeaderProvider = NetworkHeaderProvider.Empty,
    private val additionalInterceptors: List<Interceptor> = emptyList(),
    private val networkAuthenticator: Authenticator? = null,
    private val converterFactories: List<Converter.Factory> = listOf(
        GsonConverterFactory.create()
    )
) {
    init {
        if (converterFactories.isEmpty()) {
            throw NetworkConfigurationException(
                NetworkConfigurationError.EMPTY_CONVERTER_FACTORIES
            )
        }
    }

    private val retrofitByEndpoint = ConcurrentHashMap<NetworkEndpoint, Retrofit>()
    private val registeredEndpoints = ConcurrentHashMap.newKeySet<NetworkEndpoint>()
    private val okHttpClient: OkHttpClient by lazy(::createOkHttpClient)
    private val sseOkHttpClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .readTimeout(0L, TimeUnit.MILLISECONDS)
            .callTimeout(0L, TimeUnit.MILLISECONDS)
            .build()
    }
    private val webSocketOkHttpClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .pingInterval(config.webSocketPingIntervalSeconds, TimeUnit.SECONDS)
            .build()
    }

    /** 返回指定服务地址的 Retrofit；相同 Endpoint 在进程内复用同一实例。 */
    fun createRetrofit(endpoint: NetworkEndpoint): Retrofit =
        retrofitByEndpoint.computeIfAbsent(endpoint.also(registeredEndpoints::add)) {
            Retrofit.Builder()
                .baseUrl(endpoint.baseUrl)
                .client(okHttpClient)
                .apply { converterFactories.forEach(::addConverterFactory) }
                .build()
        }

    /** 使用指定 Endpoint 创建任意 Retrofit Service，Service 类型不与网络模块绑定。 */
    fun <T : Any> createService(endpoint: NetworkEndpoint, serviceClass: Class<T>): T =
        createRetrofit(endpoint).create(serviceClass)

    /** 创建绑定到指定 Endpoint 的 SSE 客户端；Flow 取消时会立即取消底层 Call。 */
    fun createSseClient(endpoint: NetworkEndpoint): SseClient =
        SseClient(sseOkHttpClient, endpoint.also(registeredEndpoints::add))

    /** 创建绑定到指定 Endpoint 的 WebSocket 客户端，并复用现有连接池和安全拦截器。 */
    fun createWebSocketClient(endpoint: NetworkEndpoint): NetworkWebSocketClient =
        NetworkWebSocketClient(
            client = webSocketOkHttpClient,
            endpoint = endpoint.also(registeredEndpoints::add)
        )

    private fun createOkHttpClient(): OkHttpClient {
        val endpointAllowlist = NetworkEndpointAllowlistInterceptor { registeredEndpoints }
        val safeRedirects = SafeRedirectInterceptor(
            isAllowed = { url -> registeredEndpoints.any { endpoint -> endpoint.permits(url) } },
            rejectedException = { url -> UnregisteredNetworkEndpointException(url.host) }
        )
        return OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(config.writeTimeoutSeconds, TimeUnit.SECONDS)
            .callTimeout(config.callTimeoutSeconds, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(false)
            .followSslRedirects(false)
            .apply {
                networkAuthenticator?.let(::authenticator)
                if (cacheDirectory != null && config.cacheSizeBytes > 0L) {
                    cache(Cache(cacheDirectory, config.cacheSizeBytes))
                }
            }
            // 应用层先拒绝恶意 @Url，避免认证头和日志接触未注册 Host。
            .addInterceptor(endpointAllowlist)
            .addInterceptor(CommonHeadersInterceptor(config.userAgent, headerProvider))
            .apply { additionalInterceptors.forEach(::addInterceptor) }
            // 关闭 OkHttp 原生自动跳转，在 DNS 与建连前校验每个 GET/HEAD 目标。
            .addInterceptor(safeRedirects)
            // 网络层作为纵深防御，再次校验每一次即将发送的实际请求。
            .addNetworkInterceptor(endpointAllowlist)
            .build()
    }
}
