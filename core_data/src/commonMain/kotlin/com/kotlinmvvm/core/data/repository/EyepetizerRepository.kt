package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.model.EyepetizerFeed
import com.kotlinmvvm.core.model.EyepetizerFeedSource

/**
 * @author 浩楠
 *
 * @date 2026-3-9
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: KMP 共享的数据仓库契约
 */
interface EyepetizerRepository {
    suspend fun getFeed(
        source: EyepetizerFeedSource = EyepetizerFeedSource.HOME_SELECTED,
        nextPageUrl: String? = null
    ): Result<EyepetizerFeed>

    suspend fun getHomeFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> =
        getFeed(EyepetizerFeedSource.HOME_SELECTED, nextPageUrl)

    suspend fun getDiscoveryFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> =
        getFeed(EyepetizerFeedSource.DISCOVERY, nextPageUrl)

    suspend fun getFollowFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> =
        getFeed(EyepetizerFeedSource.FOLLOW, nextPageUrl)

    suspend fun getDiscoveryHotFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> =
        getFeed(EyepetizerFeedSource.DISCOVERY_HOT, nextPageUrl)

    suspend fun getDiscoveryCategoryFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> =
        getFeed(EyepetizerFeedSource.DISCOVERY_CATEGORY, nextPageUrl)

    suspend fun getPgcsAllFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> =
        getFeed(EyepetizerFeedSource.PGCS_ALL, nextPageUrl)
}
