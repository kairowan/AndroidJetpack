package com.kt.NetworkModel.bean

/**
 * @author 浩楠
 *
 * @date 2023/9/14-12:35.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
data class WBanner(
    val errorCode: Int,
    val errorMsg: String,
    val datas: ArrayList<Data>,
) {
    data class Data(
        val imagePath: String, // https://www.wanandroid.com/blogimgs/42da12d8-de56-4439-b40c-eab66c227a4b.png
    )
}
