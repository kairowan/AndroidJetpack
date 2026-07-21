package com.kotlinmvvm.data.feed.local

import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy
import com.kotlinmvvm.domain.feed.model.FeedPage
import kotlinx.serialization.Serializable

/**
 * @author 浩楠
 * @date 2026/7/21 14:17
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 信息流磁盘缓存信封，携带页面快照、更新时间与格式版本供读取边界安全校验
 */
@Serializable
internal data class FeedCacheEntry(
    val items: List<FeedCacheItem>,
    val nextPageUrl: String?,
    val updatedAtEpochMillis: Long,
    val schemaVersion: Int
)

internal const val FEED_CACHE_SCHEMA_VERSION = 2

internal fun FeedCacheEntry.isCompatible(): Boolean =
    schemaVersion == FEED_CACHE_SCHEMA_VERSION && updatedAtEpochMillis >= 0L

internal fun FeedCacheEntry.toDomainPage(
    remoteResourceUrlPolicy: RemoteResourceUrlPolicy
): FeedPage = FeedPage(
    items = items.mapNotNull { item -> item.toDomain(remoteResourceUrlPolicy) },
    canLoadMore = nextPageUrl != null
)

internal fun FeedPage.toCacheEntry(
    nextPageUrl: String?,
    updatedAtEpochMillis: Long
) = FeedCacheEntry(
    items = items.map { it.toCacheItem() },
    nextPageUrl = nextPageUrl,
    updatedAtEpochMillis = updatedAtEpochMillis,
    schemaVersion = FEED_CACHE_SCHEMA_VERSION
)

internal fun isFeedCacheFresh(
    updatedAtEpochMillis: Long,
    currentTimeMillis: Long,
    freshnessMillis: Long
): Boolean {
    require(freshnessMillis >= 0L)
    if (updatedAtEpochMillis < 0L || currentTimeMillis < updatedAtEpochMillis) return false
    return currentTimeMillis - updatedAtEpochMillis <= freshnessMillis
}
