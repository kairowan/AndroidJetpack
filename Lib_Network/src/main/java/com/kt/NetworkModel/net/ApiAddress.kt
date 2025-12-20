package com.kt.network.net

class ApiAddress {

    companion object {
        /**
         * 登录接口 user/login
         */
        const val LOGIN="api/user/auth/get/verifyCode"
        /**
         * 首页文章列表
         */
        const val CALLBACK="article/list/1/json"
        /**
         * 轮播
         */
        const val BANNER="banner/json"
        /**
         * 项目分类
         */
        const val PROJECT="project/tree/json"
        /**
         * 项目分类
         */
        const val PROJECT_CONTENT="/article/list/0/json?cid=60"
    }
}