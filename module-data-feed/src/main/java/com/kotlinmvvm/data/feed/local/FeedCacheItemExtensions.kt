package com.kotlinmvvm.data.feed.local

import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedTextFooter
import com.kotlinmvvm.domain.feed.model.FeedTextHeader
import com.kotlinmvvm.domain.feed.model.FeedVideo

/** 将领域条目转换为不依赖业务运行时对象的磁盘快照。 */
internal fun FeedItem.toCacheItem(): FeedCacheItem = when (this) {
    is FeedVideo -> FeedCacheItem(
        type = TYPE_VIDEO,
        id = id,
        title = title,
        description = description,
        coverUrl = coverUrl,
        playUrl = playUrl,
        category = category,
        authorName = authorName,
        authorIconUrl = authorIconUrl,
        durationSeconds = durationSeconds
    )

    is FeedTextHeader -> FeedCacheItem(type = TYPE_TEXT_HEADER, text = text)
    is FeedTextFooter -> FeedCacheItem(type = TYPE_TEXT_FOOTER, text = text)
}

/**
 * 恢复缓存条目并重新执行远程资源安全策略；关键视频字段无效时丢弃该条目。
 */
internal fun FeedCacheItem.toDomain(remoteResourceUrlPolicy: RemoteResourceUrlPolicy): FeedItem? =
    when (type) {
        TYPE_VIDEO -> FeedVideo(
            id = id?.takeIf { it > 0 } ?: return null,
            title = title.orEmpty(),
            description = description.orEmpty(),
            coverUrl = remoteResourceUrlPolicy.normalize(coverUrl).orEmpty(),
            playUrl = remoteResourceUrlPolicy.normalize(playUrl) ?: return null,
            category = category.orEmpty(),
            authorName = authorName.orEmpty(),
            authorIconUrl = remoteResourceUrlPolicy.normalize(authorIconUrl).orEmpty(),
            durationSeconds = durationSeconds?.coerceAtLeast(0) ?: 0
        )

        TYPE_TEXT_HEADER -> text?.trim()?.takeIf(String::isNotEmpty)?.let(::FeedTextHeader)
        TYPE_TEXT_FOOTER -> text?.trim()?.takeIf(String::isNotEmpty)?.let(::FeedTextFooter)
        else -> null
    }

private const val TYPE_VIDEO = "video"
private const val TYPE_TEXT_HEADER = "text_header"
private const val TYPE_TEXT_FOOTER = "text_footer"
