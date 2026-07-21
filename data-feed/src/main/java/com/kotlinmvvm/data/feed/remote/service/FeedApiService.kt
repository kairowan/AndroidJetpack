package com.kotlinmvvm.data.feed.remote.service

import com.kotlinmvvm.data.feed.remote.model.FeedResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * @author 浩楠
 * @date 2026/7/21 12:56
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 示例 Feed 服务的 Retrofit 协议，属于业务数据模块而不是通用网络能力
 */
internal interface FeedApiService {
    /** 请求首页精选 Feed。 */
    @GET("api/v4/tabs/selected")
    suspend fun selected(): Response<FeedResponseDto>

    /** 请求发现页 Feed。 */
    @GET("api/v4/discovery")
    suspend fun discovery(): Response<FeedResponseDto>

    /** 请求关注作者 Feed。 */
    @GET("api/v4/tabs/follow")
    suspend fun follow(): Response<FeedResponseDto>

    /** 请求发现页热门内容。 */
    @GET("api/v4/discovery/hot")
    suspend fun discoveryHot(): Response<FeedResponseDto>

    /** 请求发现页分类内容。 */
    @GET("api/v4/discovery/category")
    suspend fun discoveryCategory(): Response<FeedResponseDto>

    /** 请求作者列表内容。 */
    @GET("api/v4/pgcs/all")
    suspend fun authors(): Response<FeedResponseDto>

    /** 请求服务端返回的完整分页地址。 */
    @GET
    suspend fun nextPage(@Url url: String): Response<FeedResponseDto>
}
