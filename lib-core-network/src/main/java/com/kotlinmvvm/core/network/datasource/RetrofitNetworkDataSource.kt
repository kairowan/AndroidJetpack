package com.kotlinmvvm.core.network.datasource

import com.kotlinmvvm.core.network.exception.NetworkExceptionHandler
import com.kotlinmvvm.core.network.result.NetworkResult
import com.kotlinmvvm.core.network.result.NetworkError
import com.kotlinmvvm.core.network.observer.NetworkFailureObserver
import kotlinx.coroutines.CancellationException
import retrofit2.Response

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用 NetworkDataSource 的 Retrofit 实现，统一归类协议、连接和解析异常
 */
class RetrofitNetworkDataSource(
    private val exceptionHandler: NetworkExceptionHandler = NetworkExceptionHandler(),
    private val failureObserver: NetworkFailureObserver = NetworkFailureObserver.None
) : NetworkDataSource {

    override suspend fun <T : Any> execute(request: suspend () -> Response<T>): NetworkResult<T> {
        val result = try {
            exceptionHandler.handleResponse(request())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            return NetworkError(exceptionHandler.handleException(error)).also {
                notifyFailure(it.error, error)
            }
        }
        if (result is NetworkError) notifyFailure(result.error, null)
        return result
    }

    private fun notifyFailure(failure: com.kotlinmvvm.core.network.result.NetworkFailure, cause: Throwable?) {
        try {
            failureObserver.onFailure(failure, cause)
        } catch (_: Exception) {
            // 诊断组件失败不得改变网络请求结果。
        }
    }
}
