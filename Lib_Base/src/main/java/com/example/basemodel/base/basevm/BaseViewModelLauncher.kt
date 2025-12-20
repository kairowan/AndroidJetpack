package com.example.basemodel.base.basevm

import com.kt.NetworkModel.net.requestFlow
import com.kt.network.bean.BaseResult
import com.kt.network.net.ExceptionHandle
import com.kt.network.net.ResponseThrowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * @author 浩楠
 * @date 2025/5/29 14:31
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 请求封装逻辑（launchGo、launchOnlyResult、launchFlow）
 */
interface BaseViewModelLauncher : BaseViewModelScope, BaseViewModelLiveData {

    /**
     *  不过滤请求结果
     * @param block 请求体
     * @param error 失败回调
     * @param complete  完成回调（无论成功失败都会调用）
     * @param isShowDialog 是否显示加载框
     */
    fun launchGo(
        block: suspend CoroutineScope.() -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
            uc.toastEvent().postValue("${it.code}:${it.errMsg}")
        },
        complete: suspend CoroutineScope.() -> Unit = {},
        isShowDialog: Boolean = true
    ) {
        if (isShowDialog) uc.getShowDialog().call()
        launchUI {
            handleException(
                { block() },
                { error(it) },
                {
                    uc.getDismissDialog().call()
                    complete()
                }
            )
        }
    }


    /**
     * 过滤请求结果，其他全抛异常
     * @param block 请求体
     * @param success 成功回调
     * @param error 失败回调
     * @param complete  完成回调（无论成功失败都会调用）
     * @param isShowDialog 是否显示加载框
     */
    fun <T> launchOnlyresult(
        block: suspend CoroutineScope.() -> BaseResult<T>,
        success: (T?) -> Unit,
        error: (ResponseThrowable) -> Unit = {
            uc.toastEvent().postValue(it.errMsg)
        },
        complete: () -> Unit = {},
        isShowDialog: Boolean = true
    ) {
        if (isShowDialog) uc.getShowDialog().call()
        launchUI {
            handleException({
                val result = block()
                if (result.isSuccess()) {
                    success(result.data())
                } else {
                    throw ResponseThrowable(result.errorCode(), result.errorMsg())
                }
            }, {
                error(it)
            }, {
                uc.getDismissDialog().call()
                complete()
            })
        }
    }

    /**
     * flow 运行在主线程中，可直接调用
     * @param requestCall 请求体
     * @param successBlock 成功回调
     * @param isShowDialog 是否展示加载框
     * @param errorCall 错误回调
     */
    fun <T> launchFlow(
        requestCall: suspend () -> BaseResult<T>?,
        successBlock: (T?) -> Unit,
        isShowDialog: Boolean = true,
        errorCall: (ResponseThrowable) -> Unit = {
            it.printStackTrace()
        }
    ) {
        if (isShowDialog) uc.getShowDialog().call()
        launchUI {
            val data = try {
                requestCall()?.let {
                    if (it.isSuccess()) {
                        it.data()
                    } else {
                        throw ResponseThrowable(it.errorCode(), it.errorMsg())
                    }
                }
            } catch (e: Throwable) {
                val ex = ExceptionHandle.handleException(e)
                errorCall(ex)
                null
            } finally {
                uc.getDismissDialog().call()
            }
            successBlock(data)
        }
    }
}