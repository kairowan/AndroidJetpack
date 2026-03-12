package com.kotlinmvvm.feature.home.shared

import com.kotlinmvvm.core.data.paging.PagedState
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.core.model.authorCategoryLabel
import com.kotlinmvvm.core.model.authorHandle
import com.kotlinmvvm.core.model.categoryDurationLabel

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 首页共享频道选项
 */
data class HomeFeedSourceOption(
    val key: String,
    val title: String,
    val source: EyepetizerFeedSource
)

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 首页共享视频卡片展示模型
 */
data class HomeVideoCardModel(
    val id: Int,
    val title: String,
    val descriptionText: String,
    val subtitle: String,
    val authorName: String,
    val authorHandle: String,
    val categoryDurationLabel: String,
    val authorIcon: String,
    val category: String,
    val duration: Int,
    val coverUrl: String,
    val playUrl: String
)

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 首页列表项类型
 */
enum class HomeFeedEntryType {
    VIDEO,
    HEADER,
    FOOTER
}

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 首页共享列表项
 */
data class HomeFeedEntryModel(
    val stableKey: String,
    val type: HomeFeedEntryType,
    val text: String? = null,
    val video: HomeVideoCardModel? = null
)

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 首页共享页面快照
 */
data class HomeFeedPageModel(
    val selectedSourceKey: String,
    val title: String,
    val isLoading: Boolean,
    val errorMessage: String?,
    val nextPageUrl: String?,
    val entries: List<HomeFeedEntryModel>,
    val isLoadingMore: Boolean,
    val canLoadMore: Boolean
)

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 首页共享频道目录
 */
object HomeFeedCatalog {
    val sourceOptions: List<HomeFeedSourceOption> = listOf(
        HomeFeedSourceOption(
            key = "home_selected",
            title = "精选",
            source = EyepetizerFeedSource.HOME_SELECTED
        ),
        HomeFeedSourceOption(
            key = "discovery",
            title = "发现",
            source = EyepetizerFeedSource.DISCOVERY
        ),
        HomeFeedSourceOption(
            key = "follow",
            title = "关注",
            source = EyepetizerFeedSource.FOLLOW
        ),
        HomeFeedSourceOption(
            key = "discovery_hot",
            title = "热门",
            source = EyepetizerFeedSource.DISCOVERY_HOT
        ),
        HomeFeedSourceOption(
            key = "discovery_category",
            title = "分类",
            source = EyepetizerFeedSource.DISCOVERY_CATEGORY
        ),
        HomeFeedSourceOption(
            key = "pgcs_all",
            title = "作者",
            source = EyepetizerFeedSource.PGCS_ALL
        )
    )

    val defaultSource: HomeFeedSourceOption = sourceOptions.first()

    fun sourceFromKey(sourceKey: String): EyepetizerFeedSource {
        return sourceOption(sourceKey).source
    }

    fun sourceKey(source: EyepetizerFeedSource): String {
        return sourceOptions.firstOrNull { option -> option.source == source }?.key
            ?: defaultSource.key
    }

    fun sourceOption(sourceKey: String): HomeFeedSourceOption {
        return sourceOptions.firstOrNull { option -> option.key == sourceKey } ?: defaultSource
    }
}

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 首页共享页面展示转换器
 */
object HomeFeedPagePresenter {
    fun initialState(sourceKey: String = HomeFeedCatalog.defaultSource.key): HomeFeedPageModel {
        val sourceOption = HomeFeedCatalog.sourceOption(sourceKey)
        return HomeFeedPageModel(
            selectedSourceKey = sourceOption.key,
            title = sourceOption.title,
            isLoading = true,
            errorMessage = null,
            nextPageUrl = null,
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
            nextPageUrl = null,
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
