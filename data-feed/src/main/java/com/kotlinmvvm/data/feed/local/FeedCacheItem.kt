package com.kotlinmvvm.data.feed.local

import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedTextFooter
import com.kotlinmvvm.domain.feed.model.FeedTextHeader
import com.kotlinmvvm.domain.feed.model.FeedVideo
import kotlinx.serialization.Serializable

/**
 * @author 浩楠
 * @date 2026/7/21 14:17
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 信息流磁盘条目快照，隔离序列化格式与领域模型并支持缓存格式独立演进
 */
@Serializable
internal data class FeedCacheItem(
    val type: String,
    val id: Int? = null,
    val text: String? = null,
    val title: String? = null,
    val description: String? = null,
    val coverUrl: String? = null,
    val playUrl: String? = null,
    val category: String? = null,
    val authorName: String? = null,
    val authorIconUrl: String? = null,
    val durationSeconds: Int? = null
)

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
