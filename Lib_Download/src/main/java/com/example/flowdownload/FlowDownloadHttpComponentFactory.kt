package com.example.flowdownload

import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载网络组件工厂，供宿主注入自定义 OkHttpClient、Retrofit 以及认证拦截等网络栈能力。
 */
interface FlowDownloadHttpComponentFactory {
    fun createOkHttpClient(
        config: FlowDownloadConfig,
        builder: OkHttpClient.Builder,
    ): OkHttpClient = builder.build()

    fun createRetrofit(
        config: FlowDownloadConfig,
        okHttpClient: OkHttpClient,
        builder: Retrofit.Builder,
    ): Retrofit = builder
        .client(okHttpClient)
        .build()
}

/**
 * 下载网络组件工厂注册中心，负责维护当前进程注入工厂并按类名反射恢复后台所需网络组件。
 */
object FlowDownloadHttpComponentFactories {
    @Volatile
    private var installedFactory: FlowDownloadHttpComponentFactory? = null

    fun install(factory: FlowDownloadHttpComponentFactory) {
        installedFactory = factory
    }

    fun clear() {
        installedFactory = null
    }

    fun resolve(config: FlowDownloadConfig): FlowDownloadHttpComponentFactory {
        installedFactory?.let { return it }
        val className = config.httpComponentFactoryClassName ?: return DefaultFlowDownloadHttpComponentFactory
        return instantiate(className)
    }

    private fun instantiate(className: String): FlowDownloadHttpComponentFactory {
        val clazz = Class.forName(className)
        if (!FlowDownloadHttpComponentFactory::class.java.isAssignableFrom(clazz)) {
            throw IllegalArgumentException(
                "Class $className does not implement FlowDownloadHttpComponentFactory",
            )
        }

        val singleton = runCatching {
            clazz.getField("INSTANCE").get(null)
        }.getOrNull()
        if (singleton is FlowDownloadHttpComponentFactory) {
            return singleton
        }

        val constructor = clazz.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance() as FlowDownloadHttpComponentFactory
    }
}

/**
 * 默认下载网络组件工厂，负责生成库内置的 OkHttp 和 Retrofit 基础实现。
 */
object DefaultFlowDownloadHttpComponentFactory : FlowDownloadHttpComponentFactory
