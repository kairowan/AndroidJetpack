package com.kotlinmvvm.feature.home.shared

import com.kotlinmvvm.core.model.EyepetizerFeedSource

/**
 * @author 浩楠
 * @date 2026/7/24 13:50
 * 描述: 首页共享频道目录
 */
object HomeFeedCatalog {
    val sourceOptions: List<HomeFeedSourceOption> = listOf(
        HomeFeedSourceOption("home_selected", "精选", EyepetizerFeedSource.HOME_SELECTED),
        HomeFeedSourceOption("discovery", "发现", EyepetizerFeedSource.DISCOVERY),
        HomeFeedSourceOption("follow", "关注", EyepetizerFeedSource.FOLLOW),
        HomeFeedSourceOption("discovery_hot", "热门", EyepetizerFeedSource.DISCOVERY_HOT),
        HomeFeedSourceOption("discovery_category", "分类", EyepetizerFeedSource.DISCOVERY_CATEGORY),
        HomeFeedSourceOption("pgcs_all", "作者", EyepetizerFeedSource.PGCS_ALL)
    )

    val defaultSource: HomeFeedSourceOption = sourceOptions.first()

    fun sourceFromKey(sourceKey: String): EyepetizerFeedSource = sourceOption(sourceKey).source

    fun sourceKey(source: EyepetizerFeedSource): String {
        return sourceOptions.firstOrNull { option -> option.source == source }?.key
            ?: defaultSource.key
    }

    fun sourceOption(sourceKey: String): HomeFeedSourceOption {
        return sourceOptions.firstOrNull { option -> option.key == sourceKey } ?: defaultSource
    }
}
