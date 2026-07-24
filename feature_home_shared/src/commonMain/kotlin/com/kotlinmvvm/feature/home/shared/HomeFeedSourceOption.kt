package com.kotlinmvvm.feature.home.shared

import com.kotlinmvvm.core.model.EyepetizerFeedSource

/**
 * @author 浩楠
 * @date 2026/7/24 13:50
 * 描述: 首页共享频道选项
 */
data class HomeFeedSourceOption(
    val key: String,
    val title: String,
    val source: EyepetizerFeedSource
)
