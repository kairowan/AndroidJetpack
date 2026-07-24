package com.kotlinmvvm.feature.home.shared

import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.core.state.PagedState
import com.kotlinmvvm.core.state.PagedStateHolder
import com.kotlinmvvm.domain.feed.paging.FeedPager
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author 浩楠
 * @date 2026/7/24 13:44
 * 描述: 首页跨端状态持有器
 */
class HomeFeedStateHolder(
    scope: CoroutineScope,
    repository: FeedPageRepository,
    initialSource: EyepetizerFeedSource = EyepetizerFeedSource.HOME_SELECTED
) {
    private var currentSource = initialSource
    private val mutableFeedSource = MutableStateFlow(currentSource)
    private val pager = FeedPager(
        repository = repository,
        sourceProvider = { currentSource },
        itemsMapper = { items -> items }
    )
    private val holder = PagedStateHolder(
        scope = scope,
        refreshPage = pager::refresh,
        loadNextPage = pager::loadMore
    )

    val state: StateFlow<PagedState<EyepetizerFeedItem>> = holder.state
    val feedSource: StateFlow<EyepetizerFeedSource> = mutableFeedSource.asStateFlow()

    fun loadInitial() = holder.loadInitial()

    fun refresh() = holder.refresh()

    fun retry() = holder.retry()

    fun loadMore() = holder.loadMore()

    fun switchSource(source: EyepetizerFeedSource) {
        if (source == currentSource) return
        holder.refresh(waitIfBusy = true, clearItems = true) {
            currentSource = source
            mutableFeedSource.value = source
        }
    }

    suspend fun loadInitialNow() = holder.loadInitialNow()

    suspend fun switchSourceNow(source: EyepetizerFeedSource) {
        if (source == currentSource) return
        holder.refreshNow(waitIfBusy = true, clearItems = true) {
            currentSource = source
            mutableFeedSource.value = source
        }
    }
}
