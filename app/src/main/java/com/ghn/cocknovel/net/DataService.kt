package com.ghn.cocknovel.net

import com.ghn.cocknovel.App
import com.kt.NetworkModel.bean.ProjectBean
import com.kt.NetworkModel.bean.TabFrameBean
import com.kt.NetworkModel.bean.WBanner
import com.kt.network.bean.BaseResult
import com.kt.network.bean.FontDataNew
import com.kt.network.net.ApiService
import com.kt.network.net.RetrofitClient

/**
 * @author 浩楠
 *
 * @date 2026-2-20
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

/**
 * @author 浩楠
 *
 * @date 2023/1/9
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述:请求管理器
 */
class DataService {
    companion object {
        /**
         * 登录接口
         */
        suspend fun login(
            host: Int,
            phoneNumber: String
        ): BaseResult<Any> {
            return RetrofitClient.getInstance(App.get()).getDefault(ApiService::class.java, host)
                .Login(phoneNumber)
        }

        /**
         * 首页接口
         */
        suspend fun callback(host: Int,page: Int): BaseResult<FontDataNew> {
            return RetrofitClient.getInstance(App.context()).getDefault(ApiService::class.java, host)
                .callback(page)
        }

        /**
         * banner
         */
        suspend fun wbanner(host: Int): BaseResult<MutableList<WBanner.Data>> {
            return RetrofitClient.getInstance(App.context()).getDefault(ApiService::class.java, host)
                .banner()
        }

        /**
         * 项目分类
         */
        suspend fun project(host: Int): BaseResult<MutableList<ProjectBean.Data>> {
            return RetrofitClient.getInstance(App.context()).getDefault(ApiService::class.java, host)
                .project()
        }

        /**
         * 项目分类的详情
         */
        suspend fun project_content(host: Int, page: Int, cid: Int): BaseResult<TabFrameBean.Data> {
            return RetrofitClient.getInstance(App.context()).getDefault(ApiService::class.java, host)
                .project_content(page, cid)
        }

        /**
         * 下载
         */
//        suspend fun download(host: Int, url: String): Any? {
//            return RetrofitClient.getInstance(App.context()).getDefault(ApiService::class.java, host)
//                .downloadFile(url)
//        }
    }
}