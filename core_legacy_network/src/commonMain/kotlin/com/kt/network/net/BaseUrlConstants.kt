package com.kt.network.net

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Legacy 网络 host 常量
 */
class BaseUrlConstants {
    companion object {
        private const val baseUrl1 = "http://139.224.186.198/"
        private const val baseUrl2 = "http://test2/"
        private const val video = "http://apis.juhe.cn/"
        private const val degree = "http://v.juhe.cn/"
        private const val wanandroid = "https://www.wanandroid.com"
        private const val baseurl3 = "http://rk.tongjidiaocha.com/"

        fun getHost(host: Int): String {
            return when (host) {
                1 -> baseUrl1
                2 -> baseUrl2
                3 -> video
                4 -> degree
                5 -> wanandroid
                6 -> baseurl3
                else -> baseUrl1
            }
        }
    }
}
