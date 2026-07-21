package com.kotlinmvvm.feature.home.presentation

import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.domain.feed.result.FeedLoadFailure
import com.kotlinmvvm.domain.feed.result.FeedLoadResult
import com.kotlinmvvm.domain.feed.result.FeedLoadSuccess
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.domain.feed.model.FeedSource

/**
 * @author 浩楠
 * @date 2026/7/20 17:42
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

/** 将仓库当前页面快照转换为来源切换后的完整首页状态。 */
internal fun FeedPage?.toHomeUiState(source: FeedSource) = HomeUiState(
    selectedSource = source,
    items = this?.items.orEmpty(),
    loadPhase = if (this == null) HomeLoadPhase.INITIAL else HomeLoadPhase.IDLE,
    canLoadMore = this?.canLoadMore == true
)

/** 使用仓库事实流中的完整页面更新内容，成功数据不会从请求返回值直接写入。 */
internal fun HomeUiState.withPage(page: FeedPage) = copy(
    items = page.items,
    canLoadMore = page.canLoadMore,
    loadError = null
)

/** 将单次请求结果归约为页面错误状态，内容仍以仓库 StateFlow 为唯一来源。 */
internal fun HomeUiState.withRequestResult(result: FeedLoadResult) = when (result) {
    is FeedLoadSuccess -> copy(loadError = null)
    is FeedLoadFailure -> copy(loadError = result.error)
}

/**
 * 更新唯一加载阶段。
 *
 * 无内容时刷新按首屏加载展示；缓存内容的常规加载静默执行，避免遮挡已经可用的页面。
 */
internal fun HomeUiState.withLoading(
    requestedPhase: HomeLoadPhase,
    isLoading: Boolean
): HomeUiState = copy(
    loadPhase = when {
        !isLoading -> HomeLoadPhase.IDLE
        items.isEmpty() && requestedPhase != HomeLoadPhase.NEXT_PAGE -> HomeLoadPhase.INITIAL
        items.isNotEmpty() && requestedPhase != HomeLoadPhase.INITIAL -> requestedPhase
        else -> HomeLoadPhase.IDLE
    },
    loadError = if (isLoading) null else loadError
)
