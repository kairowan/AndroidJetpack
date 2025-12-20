package com.kt.NetworkModel.net

import com.kt.network.bean.BaseResult
import com.kt.network.net.ExceptionHandle
import com.kt.network.net.ResponseThrowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
    requestCall: suspend () -> BaseResult<T>?,
): T? {
    var data: T? = null
    val flow = requestFlowResponse(errorBlock, requestCall)
    //7.调用collect获取emit()回调的结果，就是请求最后的结果
    flow.collect {
        data = it?.data
    }
    return data
}
/**
 * 通过flow执行请求，需要在协程作用域中执行
 * @param errorBlock 错误回调
 * @param requestCall 执行的请求
 * @param showLoading 开启和关闭加载框
 * @return Flow<BaseResponse<T>>
 */
suspend fun <T> requestFlowResponse(
    errorBlock: ((Int?, ResponseThrowable?) -> Unit)? = null,
    requestCall: suspend () -> BaseResult<T>?,
): Flow<BaseResult<T>?> {
    //执行请求
    val flow = flow {
        //设置超时时间
        val response = withTimeout(10 * 1000) {
            requestCall()
        }

        if (response?.success == true) {
            throw ResponseThrowable(response.errorCode, response.errorMsg)
        }
        //发送网络请求结果回调
        emit(response)
        //指定运行的线程，flow {}执行的线程
    }.flowOn(Dispatchers.IO)

        .catch { e ->
            e.printStackTrace()
            ExceptionHandle.handleException(e)
//            val exception = ExceptionHandle.handleException(e)
//            errorBlock?.invoke(exception.code, exception.errMsg)
        }
    return flow
}