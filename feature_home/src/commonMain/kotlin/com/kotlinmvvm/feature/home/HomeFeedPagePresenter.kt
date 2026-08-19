package com.kotlinmvvm.feature.home

import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.core.model.authorCategoryLabel
import com.kotlinmvvm.core.model.authorHandle
import com.kotlinmvvm.core.model.categoryDurationLabel
import com.kotlinmvvm.core.state.PagedState

/**
 * @author 浩楠
 * @date 2026/7/24 13:50
 * 描述: 首页共享页面展示转换器
 */
object HomeFeedPagePresenter {
    fun initialState(sourceKey: String = HomeFeedCatalog.defaultSource.key): HomeFeedPageModel {
        val sourceOption = HomeFeedCatalog.sourceOption(sourceKey)
        return HomeFeedPageModel(
            selectedSourceKey = sourceOption.key,
            title = sourceOption.title,
            isLoading = true,
            errorMessage = null,
            entries = emptyList(),
            isLoadingMore = false,
            canLoadMore = true
        )
    }

    fun present(
        state: PagedState<EyepetizerFeedItem>,
        selectedSource: EyepetizerFeedSource
    ): HomeFeedPageModel {
        val sourceKey = HomeFeedCatalog.sourceKey(selectedSource)
        val sourceOption = HomeFeedCatalog.sourceOption(sourceKey)
        return HomeFeedPageModel(
            selectedSourceKey = sourceKey,
            title = sourceOption.title,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            entries = state.items.map { item -> item.toEntryModel() },
            isLoadingMore = state.isLoadingMore,
            canLoadMore = state.canLoadMore
        )
    }

    private fun EyepetizerFeedItem.toEntryModel(): HomeFeedEntryModel {
        return when (this) {
            is EyepetizerFeedItem.Video -> HomeFeedEntryModel(
                stableKey = "video_$id",
                type = HomeFeedEntryType.VIDEO,
                video = HomeVideoCardModel(
                    id = id,
                    title = title,
                    descriptionText = description,
                    subtitle = authorCategoryLabel(),
                    authorName = authorName,
                    authorHandle = authorHandle(),
                    categoryDurationLabel = categoryDurationLabel(),
                    authorIcon = authorIcon,
                    category = category,
                    duration = duration,
                    coverUrl = coverUrl,
                    playUrl = playUrl
                )
            )

            is EyepetizerFeedItem.TextHeader -> HomeFeedEntryModel(
                stableKey = "header_${text.hashCode()}",
                type = HomeFeedEntryType.HEADER,
                text = text
            )

            is EyepetizerFeedItem.TextFooter -> HomeFeedEntryModel(
                stableKey = "footer_${text.hashCode()}",
                type = HomeFeedEntryType.FOOTER,
                text = text
            )
        }
    }
}
