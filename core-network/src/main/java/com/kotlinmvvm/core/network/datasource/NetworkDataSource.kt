package com.kotlinmvvm.core.network.datasource

import com.kotlinmvvm.core.network.result.NetworkResult
import retrofit2.Response

/**
 * @author 浩楠
 * @date 2026/7/20 10:38
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用 Retrofit 请求执行契约，不绑定具体服务、DTO 或业务数据源类型
 */
interface NetworkDataSource {
    /** 执行任意 Retrofit 请求，保留协程取消并返回统一的网络成功或失败类型。 */
    suspend fun <T : Any> execute(request: suspend () -> Response<T>): NetworkResult<T>
}
