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

    fun <T> create(serviceClass: Class<T>, host: NetworkHost): T {
        return create(serviceClass, host.type)
    }

    fun <T> create(serviceClass: Class<T>): T {
        return create(serviceClass, serviceClass.requireApiHost())
    }

    inline fun <reified T> create(hostType: Int): T {
        return create(T::class.java, hostType)
    }

    inline fun <reified T> create(host: NetworkHost): T {
        return create(T::class.java, host)
    }

    inline fun <reified T> create(): T {
        return create(T::class.java)
    }

    private fun <T> Class<T>.requireApiHost(): NetworkHost {
        return getAnnotation(ApiHost::class.java)?.value
            ?: throw IllegalStateException(
                "${name} 缺少 @ApiHost 声明，请在接口上标注所属的 NetworkHost"
            )
    }
}

inline fun <reified T> apiService(): T {
    return NetServiceFactory.create()
}

inline fun <reified T> apiService(hostType: Int): T {
    return NetServiceFactory.create(hostType)
}

inline fun <reified T> apiService(host: NetworkHost): T {
    return NetServiceFactory.create(host)
}
