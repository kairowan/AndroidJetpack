package com.kotlinmvvm.shared.ios

import com.kotlinmvvm.core.data.paging.PagedState
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.data.repository.EyepetizerRepositoryFactory
import com.kotlinmvvm.core.data.state.ShortsPlaybackState
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.feature.media.shared.ShortsPagePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: iOS 宿主调用的 Shorts 共享桥接层
 */
class IosShortsBridge internal constructor(
    private val repository: EyepetizerRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    constructor() : this(EyepetizerRepositoryFactory.create())

    fun initialState(): IosShortsScreenState {
        return IosShortsScreenState(
            isLoading = true,
            errorMessage = null,
            emptyMessage = "暂无视频",
            currentPage = 0,
            videos = emptyList(),
            nextPageUrl = null,
            canLoadMore = false,
            isLoadingMore = false,
            controlsCopy = IosShortsControlsCopy(
                enterPortraitFullscreenLabel = "竖屏全屏",
                enterLandscapeFullscreenLabel = "横屏全屏",
                toggleOrientationLabel = "切换横屏",
                exitFullscreenLabel = "退出全屏"
            )
        )
    }

    fun loadFeed(onResult: (IosShortsScreenState) -> Unit) {
        scope.launch {
            onResult(loadFeedNow(null))
        }
    }

    fun loadFeed(
        nextPageUrl: String?,
        onResult: (IosShortsScreenState) -> Unit
    ) {
        scope.launch {
            onResult(loadFeedNow(nextPageUrl))
        }
    }

    suspend fun loadFeedNow(nextPageUrl: String? = null): IosShortsScreenState {
        return repository.getFeed(EyepetizerFeedSource.HOME_SELECTED, nextPageUrl).fold(
            onSuccess = { feed ->
                ShortsPagePresenter.present(
                    state = PagedState(
                        items = feed.items.filterIsInstance<EyepetizerFeedItem.Video>(),
                        canLoadMore = feed.nextPageUrl != null
                    ),
                    playbackState = ShortsPlaybackState()
                ).toIosScreenState(feed.nextPageUrl)
            },
            onFailure = { error ->
                initialState().copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Feed load failed"
                )
            }
        )
    }

    fun cancel() {
        scope.cancel()
    }
}

/**
 *  描述: iOS Shorts 页面状态
 */
data class IosShortsScreenState(
    val isLoading: Boolean,
    val errorMessage: String?,
    val emptyMessage: String,
    val currentPage: Int,
    val videos: List<IosVideoCard>,
    val nextPageUrl: String?,
    val canLoadMore: Boolean,
    val isLoadingMore: Boolean,
    val controlsCopy: IosShortsControlsCopy
)

data class IosShortsControlsCopy(
    val enterPortraitFullscreenLabel: String,
    val enterLandscapeFullscreenLabel: String,
    val toggleOrientationLabel: String,
    val exitFullscreenLabel: String
)

private fun com.kotlinmvvm.feature.media.shared.ShortsPageModel.toIosScreenState(
    nextPageUrl: String?
): IosShortsScreenState {
    return IosShortsScreenState(
        isLoading = isLoading,
        errorMessage = errorMessage,
        emptyMessage = emptyMessage,
        currentPage = currentPage,
        nextPageUrl = nextPageUrl,
        canLoadMore = canLoadMore,
        isLoadingMore = isLoadingMore,
        controlsCopy = IosShortsControlsCopy(
            enterPortraitFullscreenLabel = controlsCopy.enterPortraitFullscreenLabel,
            enterLandscapeFullscreenLabel = controlsCopy.enterLandscapeFullscreenLabel,
            toggleOrientationLabel = controlsCopy.toggleOrientationLabel,
            exitFullscreenLabel = controlsCopy.exitFullscreenLabel
        ),
        videos = videos.map { video ->
            IosVideoCard(
                id = video.id,
                title = video.title,
                descriptionText = video.descriptionText,
                subtitle = video.categoryTag,
                authorName = video.authorName,
                authorHandle = video.authorHandle,
                authorIcon = video.authorIcon,
                category = video.category,
                duration = video.duration,
                categoryDurationLabel = "",
                coverUrl = video.coverUrl,
                playUrl = video.playUrl
            )
        }
    )
}
