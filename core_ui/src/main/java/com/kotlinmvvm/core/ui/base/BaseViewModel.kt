package com.kotlinmvvm.core.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinmvvm.core.ui.state.PagedData
import com.kotlinmvvm.core.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 基础 ViewModel，封装通用的状态管理逻辑
 */
abstract class BaseViewModel<T> : ViewModel() {

    protected val _uiState = MutableStateFlow<UiState<T>>(UiState.Loading)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    protected fun load(
        showLoading: Boolean = true,
        block: suspend () -> Result<T>
    ) {
        if (showLoading) {
            _uiState.value = UiState.Loading
        }
        viewModelScope.launch {
            block()
                .onSuccess { data ->
                    _uiState.value = UiState.Success(data)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.localizedMessage ?: "Unknown error")
                }
        }
    }

    protected fun updateData(transform: (T) -> T) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            _uiState.value = UiState.Success(transform(currentState.data))
        }
    }
}

/**
 * 带分页功能的 BaseViewModel
 */
abstract class BasePagedViewModel<T> : BaseViewModel<PagedData<T>>() {

    protected val allItems = mutableListOf<T>()
    protected var nextPageUrl: String? = null

    protected fun loadFirst(block: suspend () -> Result<PagedResult<T>>) {
        _uiState.value = UiState.Loading
        allItems.clear()
        nextPageUrl = null

        viewModelScope.launch {
            block()
                .onSuccess { result ->
                    allItems.addAll(result.items)
                    nextPageUrl = result.nextPageUrl
                    _uiState.value = UiState.Success(
                        PagedData(
                            items = allItems.toList(),
                            canLoadMore = !result.nextPageUrl.isNullOrEmpty()
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.localizedMessage ?: "Unknown error")
                }
        }
    }

    protected fun loadMore(block: suspend (String) -> Result<PagedResult<T>>) {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return

        val currentData = currentState.data
        if (currentData.isLoadingMore || !currentData.canLoadMore) return

        val url = nextPageUrl ?: return

        _uiState.value = UiState.Success(currentData.copy(isLoadingMore = true))

        viewModelScope.launch {
            block(url)
                .onSuccess { result ->
                    allItems.addAll(result.items)
                    nextPageUrl = result.nextPageUrl
                    _uiState.value = UiState.Success(
                        PagedData(
                            items = allItems.toList(),
                            isLoadingMore = false,
                            canLoadMore = !result.nextPageUrl.isNullOrEmpty()
                        )
                    )
                }
                .onFailure {
                    _uiState.value = UiState.Success(currentData.copy(isLoadingMore = false))
                }
        }
    }
}

data class PagedResult<T>(
    val items: List<T>,
    val nextPageUrl: String? = null
)
