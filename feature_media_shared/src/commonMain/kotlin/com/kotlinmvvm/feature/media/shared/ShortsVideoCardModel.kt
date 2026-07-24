package com.kotlinmvvm.feature.media.shared

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Shorts Screen 在所有平台消费的纯展示模型
 */
data class ShortsVideoCardModel(
    val id: Int,
    val title: String,
    val authorName: String,
    val authorHandle: String,
    val authorIcon: String,
    val category: String,
    val categoryTag: String,
    val coverUrl: String,
    val playUrl: String,
    val descriptionText: String,
    val duration: Int
)
