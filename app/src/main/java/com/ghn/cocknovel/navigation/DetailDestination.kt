package com.ghn.cocknovel.navigation

import kotlinx.serialization.Serializable

/**
 * @author 浩楠
 * @date 2026/7/24 12:08
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Navigation 3 视频详情目标，只保存恢复页面所需的稳定值
 */
@Serializable
internal data class DetailDestination(
    val videoId: Int,
    val title: String,
    val description: String,
    val coverUrl: String,
    val playUrl: String,
    val category: String,
    val authorName: String,
    val authorIcon: String,
    val duration: Int
) : AppDestination
