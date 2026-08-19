package com.kotlinmvvm.feature.shorts

import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.core.state.PagedState
import com.kotlinmvvm.core.state.PagedStateHolder
import com.kotlinmvvm.domain.feed.paging.FeedPager
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * @author 浩楠
 * @date 2026/7/24 13:44
 * 描述: Shorts 跨端 Feed 状态持有器
 */
class ShortsFeedStateHolder(
    scope: CoroutineScope,
    repository: FeedPageRepository
) {
    private val pager = FeedPager(
        repository = repository,
        sourceProvider = { EyepetizerFeedSource.HOME_SELECTED },
        itemsMapper = { items ->
            items.filterIsInstance<EyepetizerFeedItem.Video>()
        }
    )
    private val holder = PagedStateHolder(
        scope = scope,
        refreshPage = pager::refresh,
        loadNextPage = pager::loadMore
    )

    val state: StateFlow<PagedState<EyepetizerFeedItem.Video>> = holder.state

    fun loadInitial() = holder.loadInitial()

    fun refresh() = holder.refresh()

    fun retry() = holder.retry()

    fun loadMore() = holder.loadMore()

    suspend fun loadInitialNow() = holder.loadInitialNow()
}
