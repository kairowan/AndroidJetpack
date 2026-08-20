package com.kotlinmvvm.data.feed.local

import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy
import com.kotlinmvvm.domain.feed.model.FeedPage

/** 检查磁盘快照版本及时间边界，拒绝旧结构和非法时间戳。 */
internal fun FeedCacheEntry.isCompatible(): Boolean =
    schemaVersion == FEED_CACHE_SCHEMA_VERSION && updatedAtEpochMillis >= 0L

/** 将通过兼容性检查的磁盘快照恢复为页面领域模型。 */
internal fun FeedCacheEntry.toDomainPage(
    remoteResourceUrlPolicy: RemoteResourceUrlPolicy
): FeedPage = FeedPage(
    items = items.mapNotNull { item -> item.toDomain(remoteResourceUrlPolicy) },
    canLoadMore = nextPageUrl != null
)

/** 将领域页面和数据层分页信息封装为当前版本的磁盘快照。 */
internal fun FeedPage.toCacheEntry(
    nextPageUrl: String?,
    updatedAtEpochMillis: Long
) = FeedCacheEntry(
    items = items.map { it.toCacheItem() },
    nextPageUrl = nextPageUrl,
    updatedAtEpochMillis = updatedAtEpochMillis,
    schemaVersion = FEED_CACHE_SCHEMA_VERSION
)

private const val FEED_CACHE_SCHEMA_VERSION = 2
