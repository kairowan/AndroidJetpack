package com.kotlinmvvm.feature.home.shared

/**
 * @author 浩楠
 * @date 2026/7/24 13:50
 * 描述: 首页共享页面快照
 */
data class HomeFeedPageModel(
    val selectedSourceKey: String,
    val title: String,
    val isLoading: Boolean,
    val errorMessage: String?,
    val entries: List<HomeFeedEntryModel>,
    val isLoadingMore: Boolean,
    val canLoadMore: Boolean
)
