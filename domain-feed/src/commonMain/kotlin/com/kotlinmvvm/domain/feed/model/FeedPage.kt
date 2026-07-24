package com.kotlinmvvm.domain.feed.model

import com.kotlinmvvm.core.model.EyepetizerFeedItem

/**
 * @author 浩楠
 * @date 2026/7/24 13:48
 * 描述: 领域层 Feed 数据页，continuation 是不透明令牌
 */
data class FeedPage(
    val items: List<EyepetizerFeedItem>,
    val continuationToken: String?
)
