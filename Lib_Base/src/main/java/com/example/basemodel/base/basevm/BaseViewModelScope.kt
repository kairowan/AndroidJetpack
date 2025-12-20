package com.example.basemodel.base.basevm

import com.kt.network.net.ExceptionHandle
import com.kt.network.net.ResponseThrowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


/**
 * @author 浩楠
 * @date 2025/5/29 14:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO  统一管理协程作用域和异常处理
 */
interface BaseViewModelScope {

    fun launchUI(block: suspend CoroutineScope.() -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                block()
            } catch (e: Throwable) {
                // 可拓展日志记录
            }
        }
    }

    suspend fun handleException(
        block: suspend CoroutineScope.() -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit
    ) {
        coroutineScope {
            try {
                block()
            } catch (e: Throwable) {
                error(ExceptionHandle.handleException(e))
            } finally {
                complete()
            }
        }
    }
}