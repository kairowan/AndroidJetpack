package com.kotlinmvvm.core.data.network

import com.kotlinmvvm.core.data.eyepetizer.EyepetizerPayloadResponse
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * @author 浩楠
 *
 * @date 2026-3-11
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: Android 侧 Eyepetizer 专用 Retrofit 接口
 */
internal interface EyepetizerApiService {
    @GET("api/v4/tabs/selected")
    suspend fun getEyepetizerHome(): EyepetizerPayloadResponse

    @GET
    suspend fun getEyepetizerHomeMore(@Url nextPageUrl: String): EyepetizerPayloadResponse

    @GET("api/v4/discovery")
    suspend fun getEyepetizerDiscovery(): EyepetizerPayloadResponse

    @GET("api/v4/tabs/follow")
    suspend fun getEyepetizerFollow(): EyepetizerPayloadResponse

    @GET("api/v4/discovery/hot")
    suspend fun getEyepetizerDiscoveryHot(): EyepetizerPayloadResponse

    @GET("api/v4/discovery/category")
    suspend fun getEyepetizerDiscoveryCategory(): EyepetizerPayloadResponse

    @GET("api/v4/pgcs/all")
    suspend fun getEyepetizerPgcsAll(): EyepetizerPayloadResponse
}
