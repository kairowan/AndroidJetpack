package com.kotlinmvvm.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository

/**
 * @author 浩楠
 *
 * @date 2026/7/24 12:10
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 首页 Android 状态包装 ViewModel
 */
class HomeViewModel(
    repository: FeedPageRepository
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
