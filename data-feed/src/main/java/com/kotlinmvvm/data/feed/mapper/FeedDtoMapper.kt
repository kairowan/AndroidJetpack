package com.kotlinmvvm.data.feed.mapper

import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedTextFooter
import com.kotlinmvvm.domain.feed.model.FeedTextHeader
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.data.feed.remote.model.FeedDataDto
import com.kotlinmvvm.data.feed.remote.model.FeedItemDto
import com.kotlinmvvm.data.feed.remote.model.FeedRemotePage
import com.kotlinmvvm.data.feed.remote.model.FeedResponseDto

/**
 * @author 浩楠
 * @date 2026/7/21 14:17
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 递归清洗网络信息流，并在媒体 Host 策略通过后转换为与界面无关的领域分页数据
 */
internal fun FeedResponseDto.toDomain(
    remoteResourceUrlPolicy: RemoteResourceUrlPolicy,
    requestedNextPageUrl: String? = null
) = FeedRemotePage(
    page = FeedPage(
        items = itemList.flatMap { it.toDomainItems(remoteResourceUrlPolicy) },
        canLoadMore = nextPageUrl != null && nextPageUrl != requestedNextPageUrl
    ),
    nextPageUrl = nextPageUrl?.takeUnless { it == requestedNextPageUrl }
)

private fun FeedItemDto.toDomainItems(
    remoteResourceUrlPolicy: RemoteResourceUrlPolicy
): List<FeedItem> = buildList {
    data?.toTextItem(type)?.let(::add)
    toVideoItem(remoteResourceUrlPolicy)?.let(::add)

    val headerTitle = data?.header?.title
    if (!headerTitle.isNullOrBlank() && type in HEADER_CONTAINER_TYPES) {
        add(FeedTextHeader(headerTitle))
    }

    data?.itemList.orEmpty().forEach { child ->
        addAll(child.toDomainItems(remoteResourceUrlPolicy))
    }
}

private fun FeedItemDto.toVideoItem(
    remoteResourceUrlPolicy: RemoteResourceUrlPolicy
): FeedVideo? {
    val payload = data ?: return null
    if (type != TYPE_VIDEO && payload.dataType != DATA_TYPE_VIDEO) return null

    val id = payload.id?.takeIf { it > 0 } ?: return null
    val playUrl = remoteResourceUrlPolicy.normalize(payload.playUrl) ?: return null

    return FeedVideo(
        id = id,
        title = payload.title.orEmpty(),
        description = payload.description.orEmpty(),
        coverUrl = sequenceOf(payload.cover?.feed, payload.cover?.detail)
            .mapNotNull(remoteResourceUrlPolicy::normalize)
            .firstOrNull()
            .orEmpty(),
        playUrl = playUrl,
        category = payload.category.orEmpty(),
        authorName = payload.author?.name.orEmpty(),
        authorIconUrl = remoteResourceUrlPolicy.normalize(payload.author?.icon).orEmpty(),
        durationSeconds = payload.duration?.coerceAtLeast(0) ?: 0
    )
}

private fun FeedDataDto.toTextItem(type: String?): FeedItem? {
    val content = text.orEmpty().trim()
    if (content.isEmpty()) return null

    return when {
        type == TYPE_TEXT_FOOTER || dataType == DATA_TYPE_TEXT_FOOTER ->
            FeedTextFooter(content)

        type == TYPE_TEXT_HEADER || type == TYPE_LEFT_ALIGN_TEXT_HEADER ||
            dataType == DATA_TYPE_TEXT_HEADER -> FeedTextHeader(content)

        else -> null
    }
}

private const val TYPE_VIDEO = "video"
private const val TYPE_TEXT_HEADER = "textHeader"
private const val TYPE_TEXT_FOOTER = "textFooter"
private const val TYPE_LEFT_ALIGN_TEXT_HEADER = "leftAlignTextHeader"
private const val DATA_TYPE_VIDEO = "VideoBeanForClient"
private const val DATA_TYPE_TEXT_HEADER = "TextHeader"
private const val DATA_TYPE_TEXT_FOOTER = "TextFooter"
private val HEADER_CONTAINER_TYPES = setOf(
    "videoCollectionWithCover",
    "videoCollectionOfFollow",
    "videoCollectionWithBrief",
    "videoCollectionOfHorizontalScrollCard",
    "horizontalScrollCard",
    "squareCardCollection",
    "bannerCollection"
)
