package com.kotlinmvvm.feature.shorts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.core.player.defaults.ShortsPlaybackDefaults
import com.kotlinmvvm.core.player.planning.ShortsPreloadPlanner
import com.kotlinmvvm.core.player.provider.rememberPlayer
import com.kotlinmvvm.core.player.ui.PlayerSurfaceFullscreen
import com.kotlinmvvm.core.ui.base.viewModelFactory
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.feature.media.shared.FullscreenMode
import com.kotlinmvvm.feature.media.shared.ShortsPagePresenter
import com.kotlinmvvm.feature.media.shared.applyVideoWindowMode
import com.kotlinmvvm.feature.media.shared.findActivity

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Shorts Android Route，注入 ViewModel、Media3、Coil 和窗口方向能力
 */
@Composable
fun ShortsRoute(
    repository: FeedPageRepository,
    videoPlayerFactory: VideoPlayerFactory,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    deactivateSignal: Int = 0,
    providedViewModel: ShortsViewModel? = null,
    onFullscreenChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val viewModel = providedViewModel ?: viewModel(
        factory = viewModelFactory { ShortsViewModel(repository) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val pageModel = remember(state, playbackState) {
        ShortsPagePresenter.present(state, playbackState)
    }
    val player = rememberPlayer(videoPlayerFactory)
    val onFullscreenChangedState by rememberUpdatedState(onFullscreenChanged)

    LaunchedEffect(activity, playbackState.fullscreenMode) {
        activity?.applyVideoWindowMode(playbackState.fullscreenMode)
    }

    LaunchedEffect(playbackState.isFullscreen) {
        onFullscreenChangedState(playbackState.isFullscreen)
    }

    LaunchedEffect(pageModel.currentPage, pageModel.videos) {
        val video = pageModel.videos.getOrNull(pageModel.currentPage) ?: return@LaunchedEffect
        player.play(video.playUrl)
        val preloadUrls = ShortsPreloadPlanner.planUrls(
            items = pageModel.videos,
            currentPage = pageModel.currentPage,
            preloadCount = ShortsPlaybackDefaults.PRELOAD_COUNT,
            videoUrlOf = { it.playUrl }
        )
        if (preloadUrls.isNotEmpty()) player.preload(preloadUrls)
    }

    LaunchedEffect(isActive, deactivateSignal) {
        if (!isActive || deactivateSignal > 0) {
            viewModel.exitFullscreen()
            player.pause()
            player.stop()
            player.clearVideoOutput()
            onFullscreenChangedState(false)
            activity?.applyVideoWindowMode(FullscreenMode.NONE)
        }
    }

    DisposableEffect(activity) {
        onDispose {
            player.pause()
            player.stop()
            player.clearVideoOutput()
            onFullscreenChangedState(false)
            activity?.applyVideoWindowMode(FullscreenMode.NONE)
        }
    }

    BackHandler(enabled = playbackState.isFullscreen) {
        viewModel.exitFullscreen()
    }

    if (!isActive) {
        Box(modifier.fillMaxSize())
        return
    }

    ShortsScreen(
        pageModel = pageModel,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        onCurrentPageChanged = viewModel::updateCurrentPage,
        onVideoDoubleTap = player::toggle,
        onEnterPortraitFullscreen = viewModel::enterPortraitFullscreen,
        onEnterLandscapeFullscreen = viewModel::enterLandscapeFullscreen,
        onToggleFullscreenOrientation = viewModel::toggleFullscreenOrientation,
        onExitFullscreen = viewModel::exitFullscreen,
        authorImage = { video, imageModifier ->
            AsyncImage(
                model = video.authorIcon,
                contentDescription = video.authorName,
                modifier = imageModifier
            )
        },
        videoSurface = { _, isCurrent, surfaceModifier ->
            if (isCurrent) {
                PlayerSurfaceFullscreen(
                    player = player,
                    modifier = surfaceModifier
                )
            }
        },
        modifier = modifier
    )
}
