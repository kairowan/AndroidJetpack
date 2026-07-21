package com.kotlinmvvm.domain.feed.model

/**
 * @author 浩楠
 * @date 2026/7/20 15:26
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 可播放视频领域模型，只包含经过数据层校验后供业务页面使用的稳定字段
 */
data class FeedVideo(
    val id: Int,
    val title: String,
    val description: String,
    val coverUrl: String,
    val playUrl: String,
    val category: String,
    val authorName: String,
    val authorIconUrl: String,
    val durationSeconds: Int
) : FeedItem
