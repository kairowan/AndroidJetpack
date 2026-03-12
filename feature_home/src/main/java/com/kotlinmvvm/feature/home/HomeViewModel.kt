package com.kotlinmvvm.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.data.state.HomeFeedStateHolder
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource

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
 * @Description: 首页 Android 状态包装 ViewModel
 */
class HomeViewModel(
    private val repository: EyepetizerRepository
) : ViewModel() {
    private val stateHolder = HomeFeedStateHolder(
        scope = viewModelScope,
        repository = repository
    )

    val state = stateHolder.state
    val feedSource = stateHolder.feedSource

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

    fun switchSource(source: EyepetizerFeedSource) {
        stateHolder.switchSource(source)
    }

    fun findVideo(videoId: Int): EyepetizerFeedItem.Video? {
        return state.value.items
            .filterIsInstance<EyepetizerFeedItem.Video>()
            .firstOrNull { video -> video.id == videoId }
    }
}
