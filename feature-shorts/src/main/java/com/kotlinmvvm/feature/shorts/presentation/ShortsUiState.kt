package com.kotlinmvvm.feature.shorts.presentation

import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 短视频页面完整不可变状态，表达视频列表、分页进度和稳定错误类型
 */
data class ShortsUiState(
    val videos: List<FeedVideo> = emptyList(),
    val loadPhase: ShortsLoadPhase = ShortsLoadPhase.INITIAL,
    val canLoadMore: Boolean = false,
    val loadError: FeedLoadError? = null
) {
    val isInitialLoading: Boolean
        get() = loadPhase == ShortsLoadPhase.INITIAL

    val isLoadingMore: Boolean
        get() = loadPhase == ShortsLoadPhase.NEXT_PAGE
}
