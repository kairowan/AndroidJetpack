package com.kt.NetworkModel.bean


/**
 * @author 浩楠
 *
 * @date 2023/9/14-21:43.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO 项目分类
 */
data class ProjectBean(
    val data: List<Data>,
    val errorCode: Int, // 0
    val errorMsg: String
) {
    data class Data(
        val articleList: List<Any>,
        val author: String,
        val children: List<Any>,
        val courseId: Int, // 13
        val cover: String,
        val desc: String,
        val id: Int, // 294
        val lisense: String,
        val lisenseLink: String,
        val name: String, // 完整项目
        val order: Int, // 145000
        val parentChapterId: Int, // 293
        val type: Int, // 0
        val userControlSetTop: Boolean, // false
        val visible: Int // 0
    )
}