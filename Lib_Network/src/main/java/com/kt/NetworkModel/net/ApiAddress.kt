package com.kt.network.net

/**
 * @author 浩楠
 *
 * @date 2026-2-15
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

class ApiAddress {

    companion object {
        /**
         * 登录接口 user/login
         */
        const val LOGIN = "api/user/auth/get/verifyCode"

        /**
         * 首页文章列表
         */
        const val CALLBACK = "article/list/1/json"

        /**
         * 轮播
         */
        const val BANNER = "banner/json"

        /**
         * 项目分类
         */
        const val PROJECT = "project/tree/json"

        /**
         * 项目分类
         */
        const val PROJECT_CONTENT = "/article/list/0/json?cid=60"

        /**
         * 首页精选
         */
        const val EYEPETIZER_HOME_SELECTED = "api/v4/tabs/selected"
    }
}

