package com.kotlinmvvm.feature.detail

import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.categoryDurationLabel
import com.kotlinmvvm.feature.media.FullscreenMode

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 将领域视频和详情交互状态归约为跨端页面快照
 */
object VideoDetailPagePresenter {
    fun present(
        video: EyepetizerFeedItem.Video,
        state: VideoDetailState
    ): VideoDetailPageModel {
        return VideoDetailPageModel(
            title = video.title,
            playUrl = video.playUrl,
            metadataLabel = video.categoryDurationLabel(),
            authorName = video.authorName,
            authorIcon = video.authorIcon,
            descriptionText = video.description,
            isFullscreen = state.isFullscreen,
            isLandscapeFullscreen = state.fullscreenMode == FullscreenMode.LANDSCAPE,
            controlsCopy = VideoDetailControlCopy(
                enterPortraitFullscreenLabel = "竖屏全屏",
                enterLandscapeFullscreenLabel = "横屏全屏",
                toggleOrientationLabel = if (state.fullscreenMode == FullscreenMode.LANDSCAPE) {
                    "切换竖屏"
                } else {
                    "切换横屏"
                },
                exitFullscreenLabel = "退出全屏"
            )
        )
    }
}
