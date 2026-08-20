package com.kotlinmvvm.feature.home.presentation

import com.kotlinmvvm.core.data.result.DataFailure
import com.kotlinmvvm.core.data.result.DataResult
import com.kotlinmvvm.core.data.result.DataSuccess
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.domain.feed.result.FeedLoadError

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
internal fun HomeUiState.withRequestResult(
    result: DataResult<FeedPage, FeedLoadError>
) = when (result) {
    is DataSuccess -> copy(loadError = null)
    is DataFailure -> copy(loadError = result.error)
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
