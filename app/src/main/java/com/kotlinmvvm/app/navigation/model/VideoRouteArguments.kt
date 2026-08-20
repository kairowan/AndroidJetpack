package com.kotlinmvvm.app.navigation.model

import android.os.Bundle
import com.kotlinmvvm.domain.feed.model.FeedSource

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情宿主边界参数，仅传递稳定视频 ID 与可恢复的数据来源
 */
data class VideoRouteArguments(
    val videoId: Int,
    val source: FeedSource
) {
    init {
        require(videoId > 0) { "视频 ID 必须大于 0" }
    }

    /** 转换为显式 Activity 所需的最小 Bundle 参数。 */
    fun toBundle() = Bundle().apply {
        putInt(KEY_VIDEO_ID, videoId)
        putString(KEY_SOURCE, source.name)
    }

    companion object {
        /** 校验并读取 Activity 参数；参数缺失或非法时返回 null。 */
        fun from(bundle: Bundle?): VideoRouteArguments? {
            bundle ?: return null
            val videoId = bundle.getInt(KEY_VIDEO_ID)
            val source = bundle.getString(KEY_SOURCE)
                ?.let { value -> FeedSource.entries.firstOrNull { it.name == value } }
            return if (videoId > 0 && source != null) VideoRouteArguments(videoId, source) else null
        }

        private const val KEY_VIDEO_ID = "video.id"
        private const val KEY_SOURCE = "video.source"
    }
}
