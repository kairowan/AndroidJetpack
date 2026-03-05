package com.kotlinmvvm.core.data.repository

import com.kt.NetworkModel.bean.eyepetizer.FeedItem
import com.kt.NetworkModel.bean.eyepetizer.FeedItemData
import com.kotlinmvvm.core.model.EyepetizerFeed
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author 浩楠
 *
 * @date 2026-2-25
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

class EyepetizerRepository : BaseApiRepository(EYEPETIZER_HOST_TYPE) {
    suspend fun getFeed(
        source: EyepetizerFeedSource = EyepetizerFeedSource.HOME_SELECTED,
        nextPageUrl: String? = null
    ): Result<EyepetizerFeed> = withContext(Dispatchers.IO) {
        try {
            val response = if (nextPageUrl.isNullOrEmpty()) {
                when (source) {
                    EyepetizerFeedSource.HOME_SELECTED -> apiService.getEyepetizerHome()
                    EyepetizerFeedSource.DISCOVERY -> apiService.getEyepetizerDiscovery()
                    EyepetizerFeedSource.FOLLOW -> apiService.getEyepetizerFollow()
                    EyepetizerFeedSource.DISCOVERY_HOT -> apiService.getEyepetizerDiscoveryHot()
                    EyepetizerFeedSource.DISCOVERY_CATEGORY -> apiService.getEyepetizerDiscoveryCategory()
                    EyepetizerFeedSource.PGCS_ALL -> apiService.getEyepetizerPgcsAll()
                }
            } else {
                apiService.getEyepetizerHomeMore(nextPageUrl)
            }

            val items = response.itemList.orEmpty().flatMap { item ->
                item.toFeedItems()
            }

            Result.success(EyepetizerFeed(items, response.nextPageUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHomeFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> {
        return getFeed(EyepetizerFeedSource.HOME_SELECTED, nextPageUrl)
    }

    suspend fun getDiscoveryFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> {
        return getFeed(EyepetizerFeedSource.DISCOVERY, nextPageUrl)
    }

    suspend fun getFollowFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> {
        return getFeed(EyepetizerFeedSource.FOLLOW, nextPageUrl)
    }

    suspend fun getDiscoveryHotFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> {
        return getFeed(EyepetizerFeedSource.DISCOVERY_HOT, nextPageUrl)
    }

    suspend fun getDiscoveryCategoryFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> {
        return getFeed(EyepetizerFeedSource.DISCOVERY_CATEGORY, nextPageUrl)
    }

    suspend fun getPgcsAllFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> {
        return getFeed(EyepetizerFeedSource.PGCS_ALL, nextPageUrl)
    }

    private fun FeedItem.toFeedItems(): List<EyepetizerFeedItem> {
        val mappedItems = mutableListOf<EyepetizerFeedItem>()
        val data = data

        val textItem = data?.toTextItem(type)
        if (textItem != null) {
            mappedItems.add(textItem)
        }

        val videoItem = toVideoItem()
        if (videoItem != null) {
            mappedItems.add(videoItem)
        }

        val headerTitle = data?.header?.title
        if (!headerTitle.isNullOrBlank() && shouldInjectHeader(type)) {
            mappedItems.add(EyepetizerFeedItem.TextHeader(headerTitle))
        }

        data?.itemList.orEmpty().forEach { child ->
            mappedItems.addAll(child.toFeedItems())
        }

        return mappedItems
    }

    private fun FeedItem.toVideoItem(): EyepetizerFeedItem.Video? {
        val data = data ?: return null
        if (type != TYPE_VIDEO && data.dataType != DATA_TYPE_VIDEO) return null
        val id = data.id ?: return null
        val playUrl = data.playUrl.orEmpty()
        if (playUrl.isBlank()) return null

        return EyepetizerFeedItem.Video(
            id = id,
            title = data.title.orEmpty(),
            description = data.description.orEmpty(),
            coverUrl = data.cover?.feed ?: data.cover?.detail.orEmpty(),
            playUrl = playUrl,
            category = data.category.orEmpty(),
            authorName = data.author?.name.orEmpty(),
            authorIcon = data.author?.icon.orEmpty(),
            duration = data.duration ?: 0
        )
    }

    private fun FeedItemData.toTextItem(type: String?): EyepetizerFeedItem? {
        val content = text.orEmpty().trim()
        if (content.isEmpty()) return null
        return when {
            type == TYPE_TEXT_FOOTER || dataType == DATA_TYPE_TEXT_FOOTER -> {
                EyepetizerFeedItem.TextFooter(content)
            }

            type == TYPE_TEXT_HEADER || type == TYPE_LEFT_ALIGN_TEXT_HEADER || dataType == DATA_TYPE_TEXT_HEADER -> {
                EyepetizerFeedItem.TextHeader(content)
            }

            else -> null
        }
    }

    private fun shouldInjectHeader(type: String?): Boolean {
        return type in HEADER_CONTAINER_TYPES
    }

    companion object {
        private const val EYEPETIZER_HOST_TYPE = 7
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
    }
}
