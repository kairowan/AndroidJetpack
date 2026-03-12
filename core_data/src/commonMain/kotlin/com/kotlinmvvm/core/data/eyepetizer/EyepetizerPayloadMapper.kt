package com.kotlinmvvm.core.data.eyepetizer

import com.kotlinmvvm.core.model.EyepetizerFeed
import com.kotlinmvvm.core.model.EyepetizerFeedItem

internal fun EyepetizerPayloadResponse.toDomainFeed(): EyepetizerFeed {
    return EyepetizerFeed(
        items = itemList.flatMap { item -> item.toDomainItems() },
        nextPageUrl = nextPageUrl
    )
}

private fun EyepetizerPayloadItem.toDomainItems(): List<EyepetizerFeedItem> {
    val mappedItems = mutableListOf<EyepetizerFeedItem>()
    val payload = data

    payload?.toTextItem(type)?.let(mappedItems::add)
    toVideoItem()?.let(mappedItems::add)

    val headerTitle = payload?.header?.title
    if (!headerTitle.isNullOrBlank() && shouldInjectHeader(type)) {
        mappedItems += EyepetizerFeedItem.TextHeader(headerTitle)
    }

    payload?.itemList.orEmpty().forEach { child ->
        mappedItems += child.toDomainItems()
    }

    return mappedItems
}

private fun EyepetizerPayloadItem.toVideoItem(): EyepetizerFeedItem.Video? {
    val payload = data ?: return null
    if (type != TYPE_VIDEO && payload.dataType != DATA_TYPE_VIDEO) return null
    val id = payload.id ?: return null
    val playUrl = payload.playUrl.orEmpty()
    if (playUrl.isBlank()) return null

    return EyepetizerFeedItem.Video(
        id = id,
        title = payload.title.orEmpty(),
        description = payload.description.orEmpty(),
        coverUrl = payload.cover?.feed ?: payload.cover?.detail.orEmpty(),
        playUrl = playUrl,
        category = payload.category.orEmpty(),
        authorName = payload.author?.name.orEmpty(),
        authorIcon = payload.author?.icon.orEmpty(),
        duration = payload.duration ?: 0
    )
}

private fun EyepetizerPayloadData.toTextItem(type: String?): EyepetizerFeedItem? {
    val content = text.orEmpty().trim()
    if (content.isEmpty()) return null
    return when {
        type == TYPE_TEXT_FOOTER || dataType == DATA_TYPE_TEXT_FOOTER -> {
            EyepetizerFeedItem.TextFooter(content)
        }

        type == TYPE_TEXT_HEADER ||
            type == TYPE_LEFT_ALIGN_TEXT_HEADER ||
            dataType == DATA_TYPE_TEXT_HEADER -> {
            EyepetizerFeedItem.TextHeader(content)
        }

        else -> null
    }
}

private fun shouldInjectHeader(type: String?): Boolean {
    return type in HEADER_CONTAINER_TYPES
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
