package com.kotlinmvvm.feature.home

/**
 * @author 浩楠
 * @date 2026/7/24 13:50
 * 描述: 首页共享列表项
 */
data class HomeFeedEntryModel(
    val stableKey: String,
    val type: HomeFeedEntryType,
    val text: String? = null,
    val video: HomeVideoCardModel? = null
)
