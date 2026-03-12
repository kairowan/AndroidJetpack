package com.kotlinmvvm.core.data.paging

import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeed
import com.kotlinmvvm.core.model.EyepetizerFeedSource

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
 * @Description: 共享的 Feed 分页协调器
 */
class EyepetizerFeedPager<T>(
    private val repository: EyepetizerRepository,
    private val sourceProvider: () -> EyepetizerFeedSource,
    private val itemsMapper: (EyepetizerFeed) -> List<T>
) {
    private var snapshot = FeedPageSnapshot<T>()

    fun currentSnapshot(): FeedPageSnapshot<T> = snapshot

    suspend fun refresh(): Result<FeedPageSnapshot<T>> {
        snapshot = FeedPageSnapshot()
        return loadPage(nextPageUrl = null)
    }

    suspend fun loadMore(): Result<FeedPageSnapshot<T>> {
        val nextPageUrl = snapshot.nextPageUrl ?: return Result.success(snapshot)
        return loadPage(nextPageUrl)
    }

    private suspend fun loadPage(nextPageUrl: String?): Result<FeedPageSnapshot<T>> {
        return repository.getFeed(
            source = sourceProvider(),
            nextPageUrl = nextPageUrl
        ).map { feed ->
            val pageItems = itemsMapper(feed)
            val mergedItems = if (nextPageUrl.isNullOrEmpty()) {
                pageItems
            } else {
                snapshot.items + pageItems
            }
            FeedPageSnapshot(
                items = mergedItems,
                nextPageUrl = feed.nextPageUrl
            ).also { snapshot = it }
        }
    }
}

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
 * @Description: 分页快照
 */
data class FeedPageSnapshot<T>(
    val items: List<T> = emptyList(),
    val nextPageUrl: String? = null
) {
    val canLoadMore: Boolean
        get() = !nextPageUrl.isNullOrEmpty()
}
