package com.kotlinmvvm.domain.feed.paging

import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.core.state.PagedPage
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository

/**
 * @author 浩楠
 * @date 2026/7/24 13:48
 * 描述: 保存单个 Feed 会话 continuation 并合并页面
 */
class FeedPager<T>(
    private val repository: FeedPageRepository,
    private val sourceProvider: () -> EyepetizerFeedSource,
    private val itemsMapper: (List<EyepetizerFeedItem>) -> List<T>
) {
    private var items: List<T> = emptyList()
    private var continuationToken: String? = null

    suspend fun refresh(): Result<PagedPage<T>> {
        return repository.loadPage(sourceProvider(), continuationToken = null)
            .map { feed ->
                PagedPage(
                    items = itemsMapper(feed.items),
                    canLoadMore = feed.continuationToken != null
                ).also {
                    items = it.items
                    continuationToken = feed.continuationToken
                }
            }
    }

    suspend fun loadMore(): Result<PagedPage<T>> {
        val continuation = continuationToken
            ?: return Result.success(PagedPage(items = items, canLoadMore = false))

        return repository.loadPage(sourceProvider(), continuation)
            .map { feed ->
                PagedPage(
                    items = items + itemsMapper(feed.items),
                    canLoadMore = feed.continuationToken != null
                ).also {
                    items = it.items
                    continuationToken = feed.continuationToken
                }
            }
    }
}
