package com.kotlinmvvm.feature.shorts.ui.model

import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.core.player.model.ShortVideoItem

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 将业务视频模型适配为通用短视频播放器条目，不让播放器模块依赖业务模型
 */
data class ShortsVideoPlayerItem(val video: FeedVideo) : ShortVideoItem {
    override val id: Any = video.id
    override val videoUrl: String = video.playUrl
}
