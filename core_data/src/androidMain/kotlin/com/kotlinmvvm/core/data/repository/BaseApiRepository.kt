package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.data.network.ApiServiceFactory
import com.kotlinmvvm.core.data.network.EyepetizerApiService

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
 * @Description: Android 数据仓库网络基类
 */
internal abstract class BaseApiRepository {
    protected val apiService: EyepetizerApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ApiServiceFactory.createApiService()
    }
}
