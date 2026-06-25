package com.example.basemodel.base.basevm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kt.network.net.ExceptionHandle
import com.kt.network.net.ResponseThrowable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
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
        val scope = (this as? ViewModel)?.viewModelScope ?: CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e("BaseViewModelScope", "Unhandled exception in launchUI", e)
            }
        }
    }

    suspend fun handleException(
        block: suspend CoroutineScope.() -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit
    ) {
        // 直接复用当前协程上下文，避免再额外套一层 coroutineScope，
        // 防止 AOP 包裹的 suspend 调用在恢复时对已完成的子作用域重复收尾。
        val currentScope = CoroutineScope(currentCoroutineContext())
        try {
            currentScope.block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            currentScope.error(ExceptionHandle.handleException(e))
        } finally {
            currentScope.complete()
        }
    }
}
