package com.kotlinmvvm.feature.shorts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.data.state.ShortsFeedStateHolder
import com.kotlinmvvm.core.data.state.ShortsPlaybackStateHolder

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
 * @Description: Shorts Android 状态包装 ViewModel
 */
class ShortsViewModel(
    private val repository: EyepetizerRepository
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

    fun loadInitial() {
        stateHolder.loadInitial()
    }

    fun refresh() {
        stateHolder.refresh()
    }

    fun retry() {
        stateHolder.retry()
    }

    fun loadMore() {
        stateHolder.loadMore()
    }

    fun updateCurrentPage(page: Int) {
        playbackStateHolder.updateCurrentPage(page)
    }

    fun enterPortraitFullscreen() {
        playbackStateHolder.enterPortraitFullscreen()
    }

    fun enterLandscapeFullscreen() {
        playbackStateHolder.enterLandscapeFullscreen()
    }

    fun toggleFullscreenOrientation() {
        playbackStateHolder.toggleFullscreenOrientation()
    }

    fun exitFullscreen() {
        playbackStateHolder.exitFullscreen()
    }
}
