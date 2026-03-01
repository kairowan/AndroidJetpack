package com.kt.network.net

import android.annotation.SuppressLint
import android.content.Context
import com.kt.NetworkModel.net.interceptor.Level
import com.kt.NetworkModel.net.interceptor.LoggingInterceptor
import com.kt.ktmvvm.lib.BuildConfig
import com.kt.ktmvvm.net.event.OkHttpEventListener
import com.kt.network.net.dns.OkHttpDNS
import com.kt.NetworkModel.net.interceptor.HTTPDNSInterceptor
import com.kt.NetworkModel.provider.IHeaderProvider
import com.kt.network.net.interceptor.NoNetworkInterceptor
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author 浩楠
 *
 * @date 2026-2-17
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

class RetrofitClient private constructor(var context: Context) {


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var retrofitClient: RetrofitClient? = null
        private const val DEFAULT_TIME_OUT = 15
        private val sRetrofitManager: MutableMap<Int, Retrofit> = HashMap()
        private var globalHeaderProvider: IHeaderProvider? = null

        fun init(provider: IHeaderProvider) {
            this.globalHeaderProvider = provider
        }

        fun getInstance(context: Context?): RetrofitClient {
            return retrofitClient ?: synchronized(this) {
                (retrofitClient ?: context?.applicationContext?.let { RetrofitClient(it) }
                    .also { retrofitClient = it }) as RetrofitClient
            }
        }

    }


    /**
     * 创建连接客户端
     */
    private fun createOkHttpClient(optimization: Boolean, update: Boolean): OkHttpClient {
        val builder = OkHttpClient.Builder()
        var captureInterceptor: Interceptor? = null
        try {
            val clazz =
                Class.forName("com.ghn.feature.capture.interceptor.CaptureInterceptor")
            val constructor = clazz.getDeclaredConstructor()
            captureInterceptor = constructor.newInstance() as Interceptor
        } catch (e: Exception) {
            e.printStackTrace()
        }
        builder.connectTimeout(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(8, 10, TimeUnit.SECONDS)) //添加这两行代码
            .dns(OkHttpDNS.get(context))
            .eventListenerFactory(OkHttpEventListener.FACTORY)
        if (BuildConfig.DEBUG) {
            builder.sslSocketFactory(TrustAllCerts.createSSLSocketFactory()!!, TrustAllCerts())
                .hostnameVerifier(TrustAllCerts.TrustAllHostnameVerifier())
        }
        if (optimization) {
            builder.addInterceptor(HTTPDNSInterceptor(context,globalHeaderProvider))
                .cache(context?.cacheDir?.let { Cache(it, 50 * 1024 * 1024L) })//缓存目录
                .addInterceptor(NoNetworkInterceptor(context))//无网拦截器\
        }
        if (captureInterceptor != null) {
            builder.addInterceptor(captureInterceptor)
        }
        if (update==false) {
            builder.addInterceptor(LoggingInterceptor().apply {
                isDebug = BuildConfig.DEBUG
                level = Level.BASIC
                type = Platform.INFO
                requestTag = "Request"
                requestTag = "Response"
            })
        }
        return builder.build()
    }


    /**
     * 根据host 类型判断是否需要重新创建Client，因为一个app 有不同的BaseUrl，切换BaseUrl 就需要重新创建Client
     * 所以，就根据类型来从map中取出对应的client
     */
    fun <T> getDefault(interfaceServer: Class<T>?, hostType: Int): T {
        val retrofitManager = sRetrofitManager[hostType]
        return if (retrofitManager == null) {
            create(interfaceServer, hostType)
        } else retrofitManager.create(interfaceServer!!)
    }

    /**
     *
     */
    private fun <T> create(interfaceServer: Class<T>?, hostType: Int, update: Boolean = false): T {

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BaseUrlConstants.getHost(hostType))
            .addConverterFactory(GsonConverterFactory.create())
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(createOkHttpClient(true, update))
            .build()
        sRetrofitManager[hostType] = retrofit
        if (interfaceServer == null) {
            throw RuntimeException("The Api InterfaceServer is null!")
        }
        return retrofit.create(interfaceServer)
    }

}
