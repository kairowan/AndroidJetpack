package com.kotlinmvvm.core.data.state

import com.kotlinmvvm.core.data.paging.PagedFeedController
import com.kotlinmvvm.core.data.paging.PagedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * @author 浩楠
 *
 * @date 2026-3-9
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: 共享的分页状态持有器
 */
class PagedFeedStateHolder<T>(
    private val scope: CoroutineScope,
    private val controller: PagedFeedController<T>,
    initialState: PagedState<T> = PagedState()
) {
    private val requestMutex = Mutex()
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<PagedState<T>> = _state.asStateFlow()

    fun loadInitial() {
        refresh()
    }

    fun refresh(
        waitIfBusy: Boolean = false,
        beforeRefresh: (() -> Unit)? = null
    ) {
        scope.launch {
            refreshNow(
                waitIfBusy = waitIfBusy,
                beforeRefresh = beforeRefresh
            )
        }
    }

    fun retry(waitIfBusy: Boolean = false) {
        refresh(waitIfBusy = waitIfBusy)
    }

    fun loadMore() {
        scope.launch {
            loadMoreNow()
        }
    }

    suspend fun loadInitialNow() {
        refreshNow()
    }

    suspend fun refreshNow(
        waitIfBusy: Boolean = false,
        beforeRefresh: (() -> Unit)? = null
    ) {
        runRequest(waitIfBusy) {
            beforeRefresh?.invoke()
            _state.update { controller.startRefresh(it) }

            controller.refresh()
                .onSuccess { updatedState ->
                    _state.value = updatedState
                }
                .onFailure { error ->
                    _state.update { controller.errorState(it, error, loadingMore = false) }
                }
        }
    }

    suspend fun loadMoreNow() {
        runRequest(waitIfBusy = false) {
            val currentState = _state.value
            if (!controller.canLoadMore(currentState)) {
                return@runRequest
            }

            _state.update { controller.startLoadMore(it) }

            controller.loadMore(_state.value)
                .onSuccess { updatedState ->
                    _state.value = updatedState
                }
                .onFailure { error ->
                    _state.update { controller.errorState(it, error, loadingMore = true) }
                }
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
