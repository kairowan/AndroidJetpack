package com.kotlinmvvm.feature.media.shared

import com.kotlinmvvm.core.data.paging.PagedState
import com.kotlinmvvm.core.data.state.ShortsPlaybackState
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.authorHandle
import com.kotlinmvvm.core.model.categoryTag

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Shorts 共享卡片展示模型
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

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Shorts 共享控制文案
 */
data class ShortsControlCopy(
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
 *  描述: Shorts 共享页面快照
 */
data class ShortsPageModel(
    val isLoading: Boolean,
    val errorMessage: String?,
    val emptyMessage: String,
    val videos: List<ShortsVideoCardModel>,
    val currentPage: Int,
    val isFullscreen: Boolean,
    val isLandscapeFullscreen: Boolean,
    val isLoadingMore: Boolean,
    val canLoadMore: Boolean,
    val controlsCopy: ShortsControlCopy
)

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Shorts 共享展示转换器
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
            isLandscapeFullscreen = playbackState.fullscreenMode == ShortsPlaybackState.FullscreenMode.LANDSCAPE,
            isLoadingMore = state.isLoadingMore,
            canLoadMore = state.canLoadMore,
            controlsCopy = ShortsControlCopy(
                enterPortraitFullscreenLabel = "竖屏全屏",
                enterLandscapeFullscreenLabel = "横屏全屏",
                toggleOrientationLabel = if (playbackState.fullscreenMode == ShortsPlaybackState.FullscreenMode.LANDSCAPE) {
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
