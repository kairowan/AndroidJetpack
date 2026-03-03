package com.kotlinmvvm.core.data.repository

import com.kt.network.net.ApiService
import com.kotlinmvvm.core.data.network.ApiServiceFactory

/**
 * @author 浩楠
 *
 * @date 2026-3-1
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
abstract class BaseApiRepository(
    hostType: Int
) {
    protected val apiService: ApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ApiServiceFactory.createApiService(hostType)
    }
}
