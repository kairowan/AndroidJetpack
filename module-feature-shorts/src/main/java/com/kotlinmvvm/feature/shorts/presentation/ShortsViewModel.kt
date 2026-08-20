package com.kotlinmvvm.feature.shorts.presentation

import com.kotlinmvvm.core.data.result.DataFailure
import com.kotlinmvvm.core.data.result.DataResult
import com.kotlinmvvm.core.data.result.DataSuccess
import com.kotlinmvvm.core.ui.viewmodel.BaseViewModel
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.domain.feed.model.FeedVideo
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
 * 描述: 短视频状态持有者，持续观察 Feed 并管理首屏与分页请求的 Loading 状态
 */
class ShortsViewModel(
    private val pageRepository: FeedPageRepository,
    taskObserver: ViewModelTaskObserver = ViewModelTaskObserver.None
) : BaseViewModel<ShortsUiState>(ShortsUiState(), taskObserver) {

    init {
        startSource()
    }

    fun retry() {
        taskFlow { pageRepository.refreshPage(SOURCE) }
            .onEach(::handleRequestResult)
            .launchUniqueIn(
                TASK_LOAD_FEED,
                onLoadingChanged = { setLoading(ShortsLoadPhase.INITIAL, it) }
            )
    }

    fun loadMore() {
        val state = currentState
        if (!state.canLoadMore) return
        taskFlow { pageRepository.loadNextPage(SOURCE) }
            .onEach(::handleRequestResult)
            .launchUniqueIn(
                TASK_LOAD_FEED,
                onLoadingChanged = { setLoading(ShortsLoadPhase.NEXT_PAGE, it) }
            )
    }

    /** 已有内容时错误通过 Snackbar 展示，消费完成后清除瞬时状态。 */
    fun clearTransientError() {
        updateState { state ->
            if (state.videos.isNotEmpty()) state.copy(loadError = null) else state
        }
    }

    private fun startSource() {
        pageRepository.observePage(SOURCE)
            .restoreUiState { page -> page.toShortsUiState() }
            .filterNotNull()
            .onEach(::applyPage)
            .launchLatestIn(TASK_OBSERVE_FEED)

        taskFlow { pageRepository.load(SOURCE) }
            .onEach(::handleRequestResult)
            .launchLatestIn(
                TASK_LOAD_FEED,
                onLoadingChanged = { setLoading(ShortsLoadPhase.INITIAL, it) }
            )
    }

    private fun applyPage(page: FeedPage) {
        updateState {
            it.copy(
                videos = page.items.filterIsInstance<FeedVideo>(),
                canLoadMore = page.canLoadMore,
                loadError = null
            )
        }
    }

    private fun FeedPage?.toShortsUiState() = ShortsUiState(
        videos = this?.items.orEmpty().filterIsInstance<FeedVideo>(),
        loadPhase = if (this == null) ShortsLoadPhase.INITIAL else ShortsLoadPhase.IDLE,
        canLoadMore = this?.canLoadMore == true
    )

    private fun handleRequestResult(result: DataResult<FeedPage, FeedLoadError>) {
        updateState { state ->
            when (result) {
                is DataSuccess -> state.copy(loadError = null)

                is DataFailure -> state.copy(
                    loadError = result.error
                )
            }
        }
    }

    private fun setLoading(phase: ShortsLoadPhase, isLoading: Boolean) {
        updateState { state ->
            state.copy(
                loadPhase = when {
                    !isLoading -> ShortsLoadPhase.IDLE
                    state.videos.isEmpty() && phase != ShortsLoadPhase.NEXT_PAGE ->
                        ShortsLoadPhase.INITIAL
                    state.videos.isNotEmpty() && phase == ShortsLoadPhase.NEXT_PAGE ->
                        ShortsLoadPhase.NEXT_PAGE
                    else -> ShortsLoadPhase.IDLE
                },
                loadError = if (isLoading) null else state.loadError
            )
        }
    }

    private companion object {
        val SOURCE = FeedSource.HOME_SELECTED
        const val TASK_OBSERVE_FEED = "shorts.observe_feed"
        const val TASK_LOAD_FEED = "shorts.load_feed"
    }
}
