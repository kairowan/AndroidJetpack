package com.kotlinmvvm.feature.home.presentation

import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.domain.feed.result.FeedLoadError

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 首页完整不可变界面状态，统一表达首屏、刷新、分页、内容和失败状态
 */
data class HomeUiState(
    val selectedSource: FeedSource = FeedSource.HOME_SELECTED,
    val items: List<FeedItem> = emptyList(),
    val loadPhase: HomeLoadPhase = HomeLoadPhase.INITIAL,
    val canLoadMore: Boolean = false,
    val loadError: FeedLoadError? = null
) {
    val isInitialLoading: Boolean
        get() = loadPhase == HomeLoadPhase.INITIAL

    val isRefreshing: Boolean
        get() = loadPhase == HomeLoadPhase.REFRESH

    val isLoadingMore: Boolean
        get() = loadPhase == HomeLoadPhase.NEXT_PAGE
}
