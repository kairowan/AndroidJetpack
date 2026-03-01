package com.kt.network.net

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

class BaseUrlConstants {


    companion object {
        private const val baseUrl1: String = "http://139.224.186.198/"
        private const val baseUrl2: String = "http://test2/"
        private const val video: String = "http://apis.juhe.cn/"
        private const val degree: String = "http://v.juhe.cn/"
        private const val wanandroid: String = "https://www.wanandroid.com"
        private const val baseurl3: String = "http://rk.tongjidiaocha.com/"
        private const val eyepetizer: String = "http://baobab.kaiyanapp.com/"
        fun getHost(host: Int): String {
            when (host) {
                1 -> return baseUrl1
                2 -> return baseUrl2
                3 -> return video
                4 -> return degree
                5 -> return wanandroid
                6 -> return baseurl3
                7 -> return eyepetizer
            }
            return baseUrl1;
        }
    }
}