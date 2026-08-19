package com.kotlinmvvm.core.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * @author 浩楠
 * @date 2026/7/24 13:44
 * 描述: 与网络、数据库实现无关的分页状态机
 *
 * Feature 只提供刷新和下一页两个动作，状态互斥与失败恢复统一在这里处理。
 */
class PagedStateHolder<T>(
    private val scope: CoroutineScope,
    private val refreshPage: suspend () -> Result<PagedPage<T>>,
    private val loadNextPage: suspend () -> Result<PagedPage<T>>,
    private val userMessage: (Throwable) -> String = { "加载失败，请重试" },
    initialState: PagedState<T> = PagedState()
) {
    private val requestMutex = Mutex()
    private val mutableState = MutableStateFlow(initialState)

    val state: StateFlow<PagedState<T>> = mutableState.asStateFlow()

    fun loadInitial() = refresh()

    fun refresh(
        waitIfBusy: Boolean = false,
        clearItems: Boolean = false,
        beforeRefresh: (() -> Unit)? = null
    ) {
        scope.launch {
            refreshNow(
                waitIfBusy = waitIfBusy,
                clearItems = clearItems,
                beforeRefresh = beforeRefresh
            )
        }
    }

    fun retry(waitIfBusy: Boolean = false) = refresh(waitIfBusy = waitIfBusy)

    fun loadMore() {
        scope.launch {
            loadMoreNow()
        }
    }

    suspend fun loadInitialNow() = refreshNow()

    suspend fun refreshNow(
        waitIfBusy: Boolean = false,
        clearItems: Boolean = false,
        beforeRefresh: (() -> Unit)? = null
    ) {
        runRequest(waitIfBusy) {
            beforeRefresh?.invoke()
            mutableState.update { current ->
                current.copy(
                    isLoading = true,
                    items = if (clearItems) emptyList() else current.items,
                    isLoadingMore = false,
                    canLoadMore = if (clearItems) false else current.canLoadMore,
                    errorMessage = null
                )
            }

            refreshPage()
                .onSuccess(::showPage)
                .onFailure { error -> showFailure(error, loadingMore = false) }
        }
    }

    suspend fun loadMoreNow() {
        runRequest(waitIfBusy = false) {
            val current = mutableState.value
            if (current.isLoading || current.isLoadingMore || !current.canLoadMore) {
                return@runRequest
            }

            mutableState.update {
                it.copy(isLoadingMore = true, errorMessage = null)
            }
            loadNextPage()
                .onSuccess(::showPage)
                .onFailure { error -> showFailure(error, loadingMore = true) }
        }
    }

    private fun showPage(page: PagedPage<T>) {
        mutableState.value = PagedState(
            items = page.items,
            canLoadMore = page.canLoadMore
        )
    }

    private fun showFailure(error: Throwable, loadingMore: Boolean) {
        mutableState.update { current ->
            current.copy(
                isLoading = false,
                isLoadingMore = false,
                errorMessage = userMessage(error),
                canLoadMore = if (loadingMore) current.canLoadMore else {
                    current.items.isNotEmpty() && current.canLoadMore
                }
            )
        }
    }

    private suspend fun runRequest(
        waitIfBusy: Boolean,
        block: suspend () -> Unit
    ) {
        val locked = if (waitIfBusy) {
            requestMutex.lock()
            true
        } else {
            requestMutex.tryLock()
        }
        if (!locked) return

        try {
            block()
        } finally {
            requestMutex.unlock()
        }
    }
}
