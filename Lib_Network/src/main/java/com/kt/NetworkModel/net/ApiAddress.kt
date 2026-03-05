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

        /**
         * 发现页
         */
        const val EYEPETIZER_DISCOVERY = "api/v4/discovery"

        /**
         * 关注页
         */
        const val EYEPETIZER_FOLLOW = "api/v4/tabs/follow"

        /**
         * 发现-热门
         */
        const val EYEPETIZER_DISCOVERY_HOT = "api/v4/discovery/hot"

        /**
         * 发现-分类
         */
        const val EYEPETIZER_DISCOVERY_CATEGORY = "api/v4/discovery/category"

        /**
         * 发现-作者
         */
        const val EYEPETIZER_PGCS_ALL = "api/v4/pgcs/all"
    }
}
