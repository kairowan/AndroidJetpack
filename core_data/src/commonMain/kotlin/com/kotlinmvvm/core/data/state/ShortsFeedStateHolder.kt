package com.kotlinmvvm.core.data.state

import com.kotlinmvvm.core.data.paging.EyepetizerFeedPager
import com.kotlinmvvm.core.data.paging.PagedFeedController
import com.kotlinmvvm.core.data.paging.PagedState
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

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
 * @Description: Shorts Feed 的共享状态持有器
 */
class ShortsFeedStateHolder(
    scope: CoroutineScope,
    repository: EyepetizerRepository
) {
    private val holder = PagedFeedStateHolder(
        scope = scope,
        controller = PagedFeedController(
            pager = EyepetizerFeedPager(
                repository = repository,
                sourceProvider = { EyepetizerFeedSource.HOME_SELECTED },
                itemsMapper = { feed ->
                    feed.items.filterIsInstance<EyepetizerFeedItem.Video>()
                }
            )
        )
    )

    val state: StateFlow<PagedState<EyepetizerFeedItem.Video>> = holder.state

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

    suspend fun loadInitialNow() {
        holder.loadInitialNow()
    }
}
