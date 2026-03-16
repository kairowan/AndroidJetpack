package com.kt.network.net

import android.annotation.SuppressLint
import android.content.Context
import com.kt.NetworkModel.helper.NetConfigHelper
import com.kt.NetworkModel.net.interceptor.Level
import com.kt.NetworkModel.net.interceptor.LoggingInterceptor
import com.kt.NetworkModel.net.interceptor.RetryInterceptor
import com.kt.NetworkModel.net.interceptor.HTTPDNSInterceptor
import com.kt.ktmvvm.lib.BuildConfig
import com.kt.ktmvvm.net.event.OkHttpEventListener
import com.kt.network.net.dns.OkHttpDNS
import com.kt.NetworkModel.provider.IHeaderProvider
import com.kt.network.net.interceptor.NoNetworkInterceptor
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class RetrofitClient private constructor(private val context: Context) {


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var retrofitClient: RetrofitClient? = null
        private val sRetrofitManager: MutableMap<Int, Retrofit> = ConcurrentHashMap()
        private var globalHeaderProvider: IHeaderProvider? = null

        fun init(provider: IHeaderProvider) {
            this.globalHeaderProvider = provider
        }

        fun getInstance(context: Context?): RetrofitClient {
            val appContext = context?.applicationContext
                ?: throw IllegalStateException("RetrofitClient must be initialized with a non-null context")
            return retrofitClient ?: synchronized(this) {
                retrofitClient ?: RetrofitClient(appContext).also { retrofitClient = it }
            }
        }

    }


    /**
     * 创建连接客户端
     */
    private fun createOkHttpClient(update: Boolean): OkHttpClient {
        val config = NetConfigHelper.networkConfig
        return OkHttpClient.Builder()
            .applyBaseConfig(config)
            .applyDebugTlsIfNeeded()
            .applyBusinessInterceptors(config, loadCaptureInterceptor())
            .applyLogging(update)
            .build()
    }

    private fun OkHttpClient.Builder.applyBaseConfig(
        config: NetConfigHelper.NetworkConfig
    ): OkHttpClient.Builder = apply {
        connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
        writeTimeout(config.writeTimeoutSeconds, TimeUnit.SECONDS)
        readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
        connectionPool(ConnectionPool(8, 10, TimeUnit.SECONDS))
        dns(OkHttpDNS.get(context))
        eventListenerFactory(OkHttpEventListener.FACTORY)
    }

    private fun OkHttpClient.Builder.applyDebugTlsIfNeeded(): OkHttpClient.Builder = apply {
        if (!BuildConfig.DEBUG) return@apply
        val trustManager = TrustAllCerts()
        val sslSocketFactory = TrustAllCerts.createSSLSocketFactory() ?: return@apply
        sslSocketFactory(sslSocketFactory, trustManager)
        hostnameVerifier(TrustAllCerts.TrustAllHostnameVerifier())
    }

    private fun OkHttpClient.Builder.applyBusinessInterceptors(
        config: NetConfigHelper.NetworkConfig,
        captureInterceptor: Interceptor?
    ): OkHttpClient.Builder = apply {
        addInterceptorIf(config.enableHeaderInterceptor) {
            HTTPDNSInterceptor(globalHeaderProvider)
        }
        if (config.enableCache) {
            cache(Cache(context.cacheDir, config.cacheSizeBytes))
        }
        addInterceptorIf(config.enableNoNetworkInterceptor) {
            NoNetworkInterceptor(context)
        }
        addInterceptorIf(config.enableRetryInterceptor && config.retryCount > 0) {
            RetryInterceptor(config.retryCount, config.retryIntervalMillis)
        }
        captureInterceptor?.let(::addInterceptor)
    }

    private fun OkHttpClient.Builder.applyLogging(update: Boolean): OkHttpClient.Builder = apply {
        if (update) return@apply
        addNetworkInterceptor(
            LoggingInterceptor().apply {
                isDebug = BuildConfig.DEBUG
                level = Level.BASIC
                type = Platform.INFO
                requestTag = "Request"
                responseTag = "Response"
            }
        )
    }

    private fun OkHttpClient.Builder.addInterceptorIf(
        condition: Boolean,
        interceptorProvider: () -> Interceptor
    ) {
        if (condition) {
            addInterceptor(interceptorProvider())
        }
    }

    private fun loadCaptureInterceptor(): Interceptor? {
        return try {
            val clazz = Class.forName("com.ghn.feature.capture.interceptor.CaptureInterceptor")
            val constructor = clazz.getDeclaredConstructor()
            constructor.newInstance() as Interceptor
        } catch (_: Exception) {
            null
        }
    }


    /**
     * 根据host 类型判断是否需要重新创建Client，因为一个app 有不同的BaseUrl，切换BaseUrl 就需要重新创建Client
     * 所以，就根据类型来从map中取出对应的client
     */
    fun <T> getDefault(interfaceServer: Class<T>?, hostType: Int): T {
        val service = interfaceServer
            ?: throw RuntimeException("The Api InterfaceServer is null!")
        val retrofitManager = sRetrofitManager[hostType] ?: synchronized(sRetrofitManager) {
            sRetrofitManager[hostType] ?: createRetrofit(hostType).also {
                sRetrofitManager[hostType] = it
            }
        }
        return retrofitManager.create(service)
    }

    private fun createRetrofit(hostType: Int, update: Boolean = false): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BaseUrlConstants.getHost(hostType))
            .addConverterFactory(GsonConverterFactory.create())
            .client(createOkHttpClient(update))
            .build()
    }
}
