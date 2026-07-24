package com.kotlinmvvm.feature.media.shared

import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.authorHandle
import com.kotlinmvvm.core.model.categoryTag
import com.kotlinmvvm.core.state.PagedState

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 将共享 Feed 与交互状态归约为 Shorts Screen 快照
 */
object ShortsPagePresenter {
    fun present(
        state: PagedState<EyepetizerFeedItem.Video>,
        playbackState: ShortsPlaybackState
    ): ShortsPageModel {
        return ShortsPageModel(
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            emptyMessage = "暂无视频",
            videos = state.items.map { video -> video.toCardModel() },
            currentPage = playbackState.normalizedCurrentPage(state.items.size),
            isFullscreen = playbackState.isFullscreen,
            isLandscapeFullscreen = playbackState.fullscreenMode == FullscreenMode.LANDSCAPE,
            isLoadingMore = state.isLoadingMore,
            canLoadMore = state.canLoadMore,
            controlsCopy = ShortsControlCopy(
                enterPortraitFullscreenLabel = "竖屏全屏",
                enterLandscapeFullscreenLabel = "横屏全屏",
                toggleOrientationLabel = if (playbackState.fullscreenMode == FullscreenMode.LANDSCAPE) {
                    "切换竖屏"
                } else {
                    "切换横屏"
                },
                exitFullscreenLabel = "退出全屏"
            )
        )
    }

    private fun EyepetizerFeedItem.Video.toCardModel(): ShortsVideoCardModel {
        return ShortsVideoCardModel(
            id = id,
            title = title,
            authorName = authorName,
            authorHandle = authorHandle(),
            authorIcon = authorIcon,
            category = category,
            categoryTag = categoryTag(),
            coverUrl = coverUrl,
            playUrl = playUrl,
            descriptionText = description,
            duration = duration
        )
    }
}
