package com.kt.network.net

import android.content.Context

/**
 * 统一的 Retrofit Service 工厂。
 * 应用启动时初始化一次，业务模块无需重复传入 Context。
 */
object NetServiceFactory {

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun <T> create(serviceClass: Class<T>, hostType: Int): T {
        val context = appContext
            ?: throw IllegalStateException("NetServiceFactory 未初始化，请先在 Application 中调用 init(context)")
        return RetrofitClient.getInstance(context).getDefault(serviceClass, hostType)
    }

    inline fun <reified T> create(hostType: Int): T {
        return create(T::class.java, hostType)
    }
}

inline fun <reified T> apiService(hostType: Int): T {
    return NetServiceFactory.create(hostType)
}
