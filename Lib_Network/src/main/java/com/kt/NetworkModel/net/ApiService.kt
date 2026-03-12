package com.kt.network.net

import com.kt.NetworkModel.bean.ProjectBean
import com.kt.NetworkModel.bean.TabFrameBean
import com.kt.NetworkModel.bean.WBanner
import com.kt.network.bean.BaseResult
import com.kt.network.bean.FontDataNew
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * @author 浩楠
 *
 * @date 2026-2-16
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

interface ApiService {
    /**
     * 登录接口
     */
    @GET(ApiAddress.LOGIN)
    suspend fun Login(
        @Query("phoneNumber") phoneNumber: String
    ): BaseResult<Any>

    /**
     * 首页文章
     */
    @GET("article/list/{page}/json")
    suspend fun callback(@Query("page") page: Int): BaseResult<FontDataNew>

    /**
     * 轮播图
     */
    @GET(ApiAddress.BANNER)
    suspend fun banner(): BaseResult<MutableList<WBanner.Data>>

    /**
     * 项目分类
     */
    @GET(ApiAddress.PROJECT)
    suspend fun project(): BaseResult<MutableList<ProjectBean.Data>>

    /**
     * 项目列表数据
     */
    @GET("project/list/{page}/json?")
    suspend fun project_content(
        @Path("page") page: Int,
        @Query("cid") cid: Int
    ): BaseResult<TabFrameBean.Data>

    /**
     * 下载接口
     */
    @Streaming
    @GET
    suspend fun downloadFile(
        @Url url: String,
        @Header("Range") range: String
    ): Response

}
