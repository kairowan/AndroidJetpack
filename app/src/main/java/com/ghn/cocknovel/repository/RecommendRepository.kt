package com.ghn.cocknovel.repository

import com.kt.network.net.NetworkApiFactory
import com.kt.network.net.ApiService
import org.koin.core.annotation.Single

/**
 * @author 浩楠
 *
 * @date 2026/5/18
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: 推荐页数据仓库，统一封装首页相关的网络请求入口。
 */
@Single
class RecommendRepository(
    networkApiFactory: NetworkApiFactory
) {

    private val apiService: ApiService = networkApiFactory.get()

    suspend fun getBanner() = apiService.banner()

    suspend fun getHomeStatus(page: Int) = apiService.callback(page)

    suspend fun getProject() = apiService.project()

    suspend fun getProjectContent(page: Int, cid: Int) = apiService.project_content(page, cid)
}
