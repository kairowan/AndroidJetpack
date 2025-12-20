package com.kt.NetworkModel.bean



/**
 * @author 浩楠
 *
 * @date 2023/9/21-10:23.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
data class LoginBean(
    val data: List<Data>,
    val errorCode: Int, // 0
    val errorMsg: String
) {
    data class Data(
        val admin: Boolean, // false
        val chapterTops: List<Any>,
        val coinCount: Int, // 10
        val collectIds: List<Any>,
        val email: String,
        val icon: String,
        val id: Int, // 153707
        val nickname: String, // haonan1
        val password: String,
        val publicName: String, // haonan1
        val token: String,
        val type: Int, // 0
        val username: String // haonan1
    )
}