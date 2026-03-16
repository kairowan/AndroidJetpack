package com.kt.NetworkModel.net

import com.kt.NetworkModel.helper.NetConfigHelper
import com.kt.network.bean.BaseResult
import com.kt.network.net.ERROR
import com.kt.network.net.ExceptionHandle
import com.kt.network.net.ResponseThrowable
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withTimeout

/**
 * @author 浩楠
 *
 * @date 2024/4/13-11:05.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO 一个flow请求的扩展
 */

/**
 * @param errorBlock 错误回调
 * @param requestCall 执行的请求
 * @param showLoading 开启和关闭加载框
 * @return 请求结果
 *
 */
suspend fun <T> requestFlow(
    errorBlock: ((Int?, ResponseThrowable?) -> Unit)? = null,
    timeoutMillis: Long = NetConfigHelper.networkConfig.flowTimeoutMillis,
    retryCount: Long = NetConfigHelper.networkConfig.flowRetryCount,
    retryIntervalMillis: Long = NetConfigHelper.networkConfig.flowRetryIntervalMillis,
    shouldRetry: (Throwable) -> Boolean = ::defaultFlowRetryCondition,
    requestCall: suspend () -> BaseResult<T>?,
): T? {
    var data: T? = null
    val flow = requestFlowResponse(
        errorBlock = errorBlock,
        timeoutMillis = timeoutMillis,
        retryCount = retryCount,
        retryIntervalMillis = retryIntervalMillis,
        shouldRetry = shouldRetry,
        requestCall = requestCall
    )
    //7.调用collect获取emit()回调的结果，就是请求最后的结果
    flow.collect {
        data = it?.data
    }
    return data
}

/**
 * 非抛异常风格请求，方便上层通过 Result 统一处理成功/失败。
 */
suspend fun <T> requestResult(
    timeoutMillis: Long = NetConfigHelper.networkConfig.flowTimeoutMillis,
    retryCount: Long = NetConfigHelper.networkConfig.flowRetryCount,
    retryIntervalMillis: Long = NetConfigHelper.networkConfig.flowRetryIntervalMillis,
    shouldRetry: (Throwable) -> Boolean = ::defaultFlowRetryCondition,
    requestCall: suspend () -> BaseResult<T>?
): Result<T?> {
    return try {
        var data: T? = null
        requestFlowResponse(
            timeoutMillis = timeoutMillis,
            retryCount = retryCount,
            retryIntervalMillis = retryIntervalMillis,
            shouldRetry = shouldRetry,
            propagateError = true,
            requestCall = requestCall
        ).collect {
            data = it?.data
        }
        Result.success(data)
    } catch (e: Throwable) {
        if (e is CancellationException) {
            throw e
        }
        Result.failure(ExceptionHandle.handleException(e))
    }
}

/**
 * 通过flow执行请求，需要在协程作用域中执行
 * @param errorBlock 错误回调
 * @param requestCall 执行的请求
 * @return Flow<BaseResponse<T>>
 */
suspend fun <T> requestFlowResponse(
    errorBlock: ((Int?, ResponseThrowable?) -> Unit)? = null,
    timeoutMillis: Long = NetConfigHelper.networkConfig.flowTimeoutMillis,
    retryCount: Long = NetConfigHelper.networkConfig.flowRetryCount,
    retryIntervalMillis: Long = NetConfigHelper.networkConfig.flowRetryIntervalMillis,
    shouldRetry: (Throwable) -> Boolean = ::defaultFlowRetryCondition,
    propagateError: Boolean = false,
    requestCall: suspend () -> BaseResult<T>?,
): Flow<BaseResult<T>?> {
    //执行请求
    return flow {
        //设置超时时间
        val response = executeWithTimeout(timeoutMillis) {
            requestCall()
        }

        if (!response.isApiSuccess()) {
            if (response == null) {
                throw ResponseThrowable(ERROR.HTTP_ERROR.getKey(), "响应体为空")
            }
            throw ResponseThrowable(response)
        }
        //发送网络请求结果回调
        emit(response)
    }.flowOn(Dispatchers.IO)
        .retryWhen { cause, attempt ->
            if (cause is CancellationException) {
                return@retryWhen false
            }
            val canRetry = attempt < retryCount && shouldRetry(cause)
            if (canRetry && retryIntervalMillis > 0) {
                delay(retryIntervalMillis * (attempt + 1))
            }
            canRetry
        }
        .catch { e ->
            if (e is CancellationException) {
                throw e
            }
            val exception = ExceptionHandle.handleException(e)
            errorBlock?.invoke(exception.code, exception)
            if (propagateError) {
                throw exception
            }
        }
}

private suspend fun <T> executeWithTimeout(
    timeoutMillis: Long,
    requestCall: suspend () -> T
): T {
    return if (timeoutMillis > 0) {
        withTimeout(timeoutMillis) { requestCall() }
    } else {
        requestCall()
    }
}

private fun defaultFlowRetryCondition(throwable: Throwable): Boolean {
    return throwable is SocketTimeoutException ||
        throwable is ConnectException ||
        throwable is UnknownHostException ||
        throwable is SSLException ||
        throwable is IOException
}

private fun BaseResult<*>?.isApiSuccess(): Boolean {
    if (this == null) return false
    return success || isSuccess()
}
