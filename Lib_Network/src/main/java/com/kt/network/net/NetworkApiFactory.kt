package com.kt.network.net

import java.util.concurrent.ConcurrentHashMap

/**
 * @author 浩楠
 *
 * @date 2026/5/18
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: 统一管理 Retrofit 接口实例，业务层按类型获取接口，减少重复的 Koin Provider 定义
 */
class NetworkApiFactory {

    private val serviceCache = ConcurrentHashMap<Class<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(serviceClass: Class<T>): T {
        return serviceCache[serviceClass] as? T ?: synchronized(serviceCache) {
            serviceCache[serviceClass] as? T
                ?: NetServiceFactory.create(serviceClass).also { serviceCache[serviceClass] = it as Any }
        }
    }

    inline fun <reified T : Any> get(): T {
        return get(T::class.java)
    }
}
