package com.example.basemodel.base.basevm

import com.kt.network.net.requestFlow
import com.kt.network.bean.BaseResult
import com.kt.network.net.ExceptionHandle
import com.kt.network.net.ResponseThrowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

interface BaseViewModelLauncher : BaseViewModelScope, BaseViewModelLiveData {

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
                    success(result.data)
                } else {
                    throw ResponseThrowable(result.errorCode, result.errorMsg)
                }
            }, {
                error(it)
            }, {
                uc.getDismissDialog().call()
                complete()
            })
        }
    }

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
            try {
                val data = requestCall()?.let {
                    if (it.isSuccess()) {
                        it.data
                    } else {
                        throw ResponseThrowable(it.errorCode, it.errorMsg)
                    }
                }
                successBlock(data)
            } catch (e: Throwable) {
                val ex = ExceptionHandle.handleException(e)
                errorCall(ex)
            } finally {
                uc.getDismissDialog().call()
            }
        }
    }
}
