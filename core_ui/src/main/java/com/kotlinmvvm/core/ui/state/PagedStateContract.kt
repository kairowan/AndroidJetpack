package com.kotlinmvvm.core.ui.state

/**
 * 通用分页页面 State。
 */
data class PagedState<T>(
    val isLoading: Boolean = false,
    val items: List<T> = emptyList(),
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val errorMessage: String? = null
)
