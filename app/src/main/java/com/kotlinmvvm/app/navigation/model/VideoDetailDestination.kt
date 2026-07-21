package com.kotlinmvvm.app.navigation.model

import com.kotlinmvvm.domain.feed.model.FeedSource
import kotlinx.serialization.Serializable

/**
 * @author 浩楠
 * @date 2026/7/20 15:26
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情导航目的地，只携带可持久化的视频编号与信息流来源
 */
@Serializable
data class VideoDetailDestination(
    val videoId: Int,
    val sourceName: String
) : AppRoute {
    constructor(videoId: Int, source: FeedSource) : this(videoId, source.name)

    init {
        require(videoId > 0) { "视频 ID 必须大于 0" }
        require(FeedSource.entries.any { it.name == sourceName }) { "信息流来源无效: $sourceName" }
    }

    val source: FeedSource
        get() = FeedSource.valueOf(sourceName)
}
