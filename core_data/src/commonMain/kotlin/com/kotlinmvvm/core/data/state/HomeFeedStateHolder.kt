package com.kotlinmvvm.core.data.state

import com.kotlinmvvm.core.data.paging.EyepetizerFeedPager
import com.kotlinmvvm.core.data.paging.PagedFeedController
import com.kotlinmvvm.core.data.paging.PagedState
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
 * @Description: 首页 Feed 的共享状态持有器
 */
class HomeFeedStateHolder(
    scope: CoroutineScope,
    repository: EyepetizerRepository,
    initialSource: EyepetizerFeedSource = EyepetizerFeedSource.HOME_SELECTED
) {
    private var currentSource = initialSource
    private val _feedSource = MutableStateFlow(currentSource)
    private val holder = PagedFeedStateHolder(
        scope = scope,
        controller = PagedFeedController(
            pager = EyepetizerFeedPager(
                repository = repository,
                sourceProvider = { currentSource },
                itemsMapper = { feed -> feed.items }
            )
        )
    )

    val state: StateFlow<PagedState<EyepetizerFeedItem>> = holder.state
    val feedSource: StateFlow<EyepetizerFeedSource> = _feedSource.asStateFlow()

    fun loadInitial() {
        holder.loadInitial()
    }

    fun refresh() {
        holder.refresh()
    }

    fun retry() {
        holder.retry()
    }

    fun loadMore() {
        holder.loadMore()
    }

    fun switchSource(source: EyepetizerFeedSource) {
        if (source == currentSource) return
        holder.refresh(waitIfBusy = true) {
            currentSource = source
            _feedSource.value = source
        }
    }

    suspend fun loadInitialNow() {
        holder.loadInitialNow()
    }

    suspend fun switchSourceNow(source: EyepetizerFeedSource) {
        if (source == currentSource) return
        holder.refreshNow(waitIfBusy = true) {
            currentSource = source
            _feedSource.value = source
        }
    }
}
