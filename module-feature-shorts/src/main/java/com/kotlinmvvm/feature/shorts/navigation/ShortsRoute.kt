package com.kotlinmvvm.feature.shorts.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.core.player.provider.rememberVideoPlayerController
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.feature.shorts.presentation.ShortsViewModel
import com.kotlinmvvm.feature.shorts.ui.ShortsScreen
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 短视频路由入口，协调页面状态和播放器并向外发送窗口模式请求
 */
@Composable
fun ShortsRoute(
    pageRepository: FeedPageRepository,
    videoPlayerFactory: VideoPlayerFactory,
    onFullscreenChanged: (Boolean) -> Unit,
    onWindowModeChanged: (VideoWindowMode) -> Unit,
    taskObserver: ViewModelTaskObserver = ViewModelTaskObserver.None,
    modifier: Modifier = Modifier
) {
    val viewModel: ShortsViewModel = viewModel { ShortsViewModel(pageRepository, taskObserver) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val player = rememberVideoPlayerController(videoPlayerFactory)
    val latestFullscreenCallback by rememberUpdatedState(onFullscreenChanged)
    val latestWindowCallback by rememberUpdatedState(onWindowModeChanged)
    var windowModeName by rememberSaveable { mutableStateOf(VideoWindowMode.INLINE.name) }
    val windowMode = remember(windowModeName) { VideoWindowMode.valueOf(windowModeName) }
    val isFullscreen = windowMode != VideoWindowMode.INLINE

    LaunchedEffect(windowMode) { latestWindowCallback(windowMode) }
    LaunchedEffect(isFullscreen) { latestFullscreenCallback(isFullscreen) }
    DisposableEffect(player) {
        onDispose {
            player.stop()
            latestWindowCallback(VideoWindowMode.INLINE)
            latestFullscreenCallback(false)
        }
    }
    BackHandler(enabled = isFullscreen) { windowModeName = VideoWindowMode.INLINE.name }

    ShortsScreen(
        uiState = uiState,
        player = player,
        windowMode = windowMode,
        onWindowModeChanged = { windowModeName = it.name },
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        onErrorShown = viewModel::clearTransientError,
        modifier = modifier
    )
}
