package com.kotlinmvvm.feature.home.presentation

import androidx.lifecycle.SavedStateHandle
import com.kotlinmvvm.core.data.result.DataResult
import com.kotlinmvvm.core.ui.viewmodel.BaseViewModel
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.domain.feed.model.FeedSource
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 首页状态持有者，管理来源观察以及首屏、刷新、分页请求的并发与 Loading 状态
 */
class HomeViewModel(
    private val pageRepository: FeedPageRepository,
    private val savedStateHandle: SavedStateHandle,
    taskObserver: ViewModelTaskObserver = ViewModelTaskObserver.None
) : BaseViewModel<HomeUiState>(initialState(savedStateHandle), taskObserver) {

    init {
        startSource(currentState.selectedSource)
    }

    fun selectSource(source: FeedSource) {
        if (source == currentState.selectedSource) return
        savedStateHandle[KEY_SELECTED_SOURCE] = source.name
        startSource(source)
    }

    fun refresh() {
        val source = currentState.selectedSource
        taskFlow { pageRepository.refreshPage(source) }
            .onEach { result -> applyRequestResult(source, result) }
            .launchUniqueIn(
                taskKey = TASK_REQUEST_PAGE,
                onLoadingChanged = { setLoading(HomeLoadPhase.REFRESH, it) }
            )
    }

    fun retry() = refresh()

    fun loadMore() {
        val state = currentState
        if (!state.canLoadMore) return
        val source = state.selectedSource
        taskFlow { pageRepository.loadNextPage(source) }
            .onEach { result -> applyRequestResult(source, result) }
            .launchUniqueIn(
                taskKey = TASK_REQUEST_PAGE,
                onLoadingChanged = { setLoading(HomeLoadPhase.NEXT_PAGE, it) }
            )
    }

    /** 内容存在时错误通过 Snackbar 展示，展示完成后从页面状态中移除。 */
    fun clearTransientError() {
        updateState { state ->
            if (state.items.isNotEmpty()) state.copy(loadError = null) else state
        }
    }

    private fun startSource(source: FeedSource) {
        pageRepository.observePage(source)
            .restoreUiState { page -> page.toHomeUiState(source) }
            .filterNotNull()
            .filter { currentState.selectedSource == source }
            .onEach { page -> updateState { state -> state.withPage(page) } }
            .launchLatestIn(TASK_OBSERVE_PAGE)

        taskFlow { pageRepository.load(source) }
            .onEach { result -> applyRequestResult(source, result) }
            .launchLatestIn(
                taskKey = TASK_REQUEST_PAGE,
                onLoadingChanged = { setLoading(HomeLoadPhase.INITIAL, it) }
            )
    }

    private fun applyRequestResult(
        source: FeedSource,
        result: DataResult<FeedPage, FeedLoadError>
    ) {
        if (currentState.selectedSource != source) return
        updateState { state -> state.withRequestResult(result) }
    }

    private fun setLoading(phase: HomeLoadPhase, isLoading: Boolean) {
        updateState { state -> state.withLoading(phase, isLoading) }
    }

    private companion object {
        const val KEY_SELECTED_SOURCE = "home.selected_source"
        const val TASK_OBSERVE_PAGE = "home.observe_page"
        const val TASK_REQUEST_PAGE = "home.request_page"

        fun initialState(savedStateHandle: SavedStateHandle): HomeUiState = HomeUiState(
            selectedSource = savedStateHandle.get<String>(KEY_SELECTED_SOURCE)
                ?.let { name -> FeedSource.entries.firstOrNull { it.name == name } }
                ?: FeedSource.HOME_SELECTED
        )
    }
}
