package com.kotlinmvvm.feature.media.shared

import com.kotlinmvvm.core.data.state.VideoDetailState
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.categoryDurationLabel

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 详情页共享控制文案
 */
data class VideoDetailControlCopy(
    val enterPortraitFullscreenLabel: String,
    val enterLandscapeFullscreenLabel: String,
    val toggleOrientationLabel: String,
    val exitFullscreenLabel: String
)

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 详情页共享页面快照
 */
data class VideoDetailPageModel(
    val title: String,
    val playUrl: String,
    val metadataLabel: String,
    val authorName: String,
    val authorIcon: String,
    val descriptionText: String,
    val isFullscreen: Boolean,
    val isLandscapeFullscreen: Boolean,
    val controlsCopy: VideoDetailControlCopy
)

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 详情页共享展示转换器
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
            isLandscapeFullscreen = state.fullscreenMode == VideoDetailState.FullscreenMode.LANDSCAPE,
            controlsCopy = VideoDetailControlCopy(
                enterPortraitFullscreenLabel = "竖屏全屏",
                enterLandscapeFullscreenLabel = "横屏全屏",
                toggleOrientationLabel = if (state.fullscreenMode == VideoDetailState.FullscreenMode.LANDSCAPE) {
                    "切换竖屏"
                } else {
                    "切换横屏"
                },
                exitFullscreenLabel = "退出全屏"
            )
        )
    }
}
