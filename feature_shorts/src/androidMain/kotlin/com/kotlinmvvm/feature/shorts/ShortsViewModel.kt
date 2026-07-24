package com.kotlinmvvm.feature.shorts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.feature.media.shared.ShortsFeedStateHolder
import com.kotlinmvvm.feature.media.shared.ShortsPlaybackStateHolder

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Shorts Android 生命周期包装，业务和播放状态仍由 KMP 状态机持有
 */
class ShortsViewModel(
    repository: FeedPageRepository
) : ViewModel() {
    private val stateHolder = ShortsFeedStateHolder(
        scope = viewModelScope,
        repository = repository
    )
    private val playbackStateHolder = ShortsPlaybackStateHolder()

    val state = stateHolder.state
    val playbackState = playbackStateHolder.state

    init {
        stateHolder.loadInitial()
    }

    fun refresh() = stateHolder.refresh()

    fun retry() = stateHolder.retry()

    fun loadMore() = stateHolder.loadMore()

    fun updateCurrentPage(page: Int) = playbackStateHolder.updateCurrentPage(page)

    fun enterPortraitFullscreen() = playbackStateHolder.enterPortraitFullscreen()

    fun enterLandscapeFullscreen() = playbackStateHolder.enterLandscapeFullscreen()

    fun toggleFullscreenOrientation() = playbackStateHolder.toggleFullscreenOrientation()

    fun exitFullscreen() = playbackStateHolder.exitFullscreen()
}
