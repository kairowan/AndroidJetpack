package com.kotlinmvvm.data.feed.remote

import com.kotlinmvvm.data.feed.remote.model.FeedResponseDto
import com.kotlinmvvm.core.network.result.NetworkResult

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Feed 业务远程数据源契约，为仓库屏蔽 Retrofit Service 和请求执行细节
 */
internal interface FeedRemoteDataSource {
    /** 加载首页精选 Feed，失败时返回可分类的网络错误。 */
    suspend fun fetchSelected(): NetworkResult<FeedResponseDto>

    /** 加载发现页 Feed，失败时返回可分类的网络错误。 */
    suspend fun fetchDiscovery(): NetworkResult<FeedResponseDto>

    /** 加载关注作者 Feed，失败时返回可分类的网络错误。 */
    suspend fun fetchFollow(): NetworkResult<FeedResponseDto>

    /** 加载热门 Feed，失败时返回可分类的网络错误。 */
    suspend fun fetchHot(): NetworkResult<FeedResponseDto>

    /** 加载分类 Feed，失败时返回可分类的网络错误。 */
    suspend fun fetchCategories(): NetworkResult<FeedResponseDto>

    /** 加载作者 Feed，失败时返回可分类的网络错误。 */
    suspend fun fetchAuthors(): NetworkResult<FeedResponseDto>

    /** 使用服务端 continuation URL 加载下一页，不改变当前 Feed 来源。 */
    suspend fun fetchNextPage(url: String): NetworkResult<FeedResponseDto>
}
