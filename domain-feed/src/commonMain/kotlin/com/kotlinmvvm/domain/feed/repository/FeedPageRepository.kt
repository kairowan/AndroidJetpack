package com.kotlinmvvm.domain.feed.repository

import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.domain.feed.model.FeedPage

/**
 * @author 浩楠
 * @date 2026/7/24 13:48
 * 描述: Feature 可依赖的 Feed 分页读取角色
 */
interface FeedPageRepository {
    /**
     * 按频道和不透明 continuation 读取一页 Feed。
     */
    suspend fun loadPage(
        source: EyepetizerFeedSource = EyepetizerFeedSource.HOME_SELECTED,
        continuationToken: String? = null
    ): Result<FeedPage>
}
