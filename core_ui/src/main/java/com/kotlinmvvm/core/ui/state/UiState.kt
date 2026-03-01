package com.kotlinmvvm.core.ui.state

/**
 * @author 浩楠
 *
 * @date 2026-2-27
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

/**
 * 通用 UI 状态封装
 */
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/**
 * 分页数据封装
 */
data class PagedData<T>(
    val items: List<T>,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true
)

/**
 * 扩展函数：判断是否加载中
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * 扩展函数：判断是否成功
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * 扩展函数：判断是否错误
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * 扩展函数：获取数据（成功时）
 */
fun <T> UiState<T>.getOrNull(): T? = (this as? UiState.Success)?.data

/**
 * 扩展函数：获取错误信息
 */
fun <T> UiState<T>.errorMessageOrNull(): String? = (this as? UiState.Error)?.message
