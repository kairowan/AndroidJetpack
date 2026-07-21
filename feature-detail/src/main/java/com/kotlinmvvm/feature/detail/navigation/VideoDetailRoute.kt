package com.kotlinmvvm.feature.detail.navigation

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.core.player.provider.rememberVideoPlayerController
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.core.ui.feedback.ErrorContent
import com.kotlinmvvm.core.ui.feedback.LoadingContent
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver
import com.kotlinmvvm.domain.feed.repository.FeedVideoRepository
import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.feature.detail.R
import com.kotlinmvvm.feature.detail.presentation.VideoDetailViewModel
import com.kotlinmvvm.feature.detail.ui.VideoDetailScreen
import com.kotlinmvvm.feature.detail.ui.model.BrandedPlayerControlsConfig
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情路由入口，按 ID 获取页面状态并将窗口模式请求交给 Activity 宿主
 */
@Composable
fun VideoDetailRoute(
    videoId: Int,
    source: FeedSource,
    videoRepository: FeedVideoRepository,
    videoPlayerFactory: VideoPlayerFactory,
    onBack: () -> Unit,
    onWindowModeChanged: (VideoWindowMode) -> Unit,
    taskObserver: ViewModelTaskObserver = ViewModelTaskObserver.None,
    modifier: Modifier = Modifier
) {
    val viewModel: VideoDetailViewModel = viewModel {
        VideoDetailViewModel(videoRepository, videoId, source, taskObserver)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val video = uiState.video
    when {
        uiState.isLoading -> LoadingContent(
            modifier.windowInsetsPadding(WindowInsets.safeDrawing)
        )
        video != null -> VideoDetailContent(
            video = video,
            videoPlayerFactory = videoPlayerFactory,
            onBack = onBack,
            onWindowModeChanged = onWindowModeChanged,
            modifier = modifier.windowInsetsPadding(WindowInsets.safeDrawing)
        )
        else -> ErrorContent(
            message = stringResource(
                uiState.loadError?.messageResource ?: R.string.detail_error_unknown
            ),
            onRetry = viewModel::retry,
            modifier = modifier
        )
    }
}

@Composable
private fun VideoDetailContent(
    video: FeedVideo,
    videoPlayerFactory: VideoPlayerFactory,
    onBack: () -> Unit,
    onWindowModeChanged: (VideoWindowMode) -> Unit,
    modifier: Modifier
) {
    val player = rememberVideoPlayerController(videoPlayerFactory)
    val latestWindowCallback by rememberUpdatedState(onWindowModeChanged)
    var windowModeName by rememberSaveable { mutableStateOf(VideoWindowMode.INLINE.name) }
    val windowMode = remember(windowModeName) { VideoWindowMode.valueOf(windowModeName) }
    val controlsConfig = remember {
        BrandedPlayerControlsConfig(autoHideMs = 2_600L, replayMs = 8_000L, forwardMs = 12_000L)
    }
    var savedPositionMs by rememberSaveable(video.id) { mutableLongStateOf(0L) }
    var savedPlaybackRequested by rememberSaveable(video.id) { mutableStateOf(true) }
    var savedSpeed by rememberSaveable(video.id) { mutableFloatStateOf(1f) }

    LaunchedEffect(player, video.id, video.playUrl) {
        player.play(video.playUrl, autoPlay = false)
        player.setSpeed(savedSpeed)
        savedPositionMs.takeIf { it > 0L }?.let(player::seekTo)
        if (savedPlaybackRequested) player.resume()
    }
    LaunchedEffect(player, video.id) {
        player.state
            .map { state -> Triple(state.position / 1_000L, state.playbackRequested, state.speed) }
            .distinctUntilChanged()
            .collect {
                val state = player.state.value
                if (state.position > 0L) savedPositionMs = state.position
                savedPlaybackRequested = state.playbackRequested
                savedSpeed = state.speed
            }
    }
    LaunchedEffect(windowMode) { latestWindowCallback(windowMode) }
    DisposableEffect(player, video.id) {
        onDispose {
            val state = player.state.value
            savedPositionMs = state.position
            savedPlaybackRequested = state.playbackRequested
            savedSpeed = state.speed
            latestWindowCallback(VideoWindowMode.INLINE)
        }
    }
    BackHandler(enabled = windowMode != VideoWindowMode.INLINE) {
        windowModeName = VideoWindowMode.INLINE.name
    }

    VideoDetailScreen(
        video = video,
        player = player,
        windowMode = windowMode,
        controlsConfig = controlsConfig,
        onBack = onBack,
        onWindowModeChanged = { windowModeName = it.name },
        modifier = modifier
    )
}

@get:StringRes
private val FeedLoadError.messageResource: Int
    get() = when (this) {
        FeedLoadError.NO_CONNECTION -> R.string.detail_error_no_connection
        FeedLoadError.TIMEOUT -> R.string.detail_error_timeout
        FeedLoadError.UNAUTHORIZED -> R.string.detail_error_unauthorized
        FeedLoadError.SERVER -> R.string.detail_error_server
        FeedLoadError.INVALID_RESPONSE -> R.string.detail_error_invalid_response
        FeedLoadError.NOT_FOUND -> R.string.detail_error_not_found
        FeedLoadError.UNKNOWN -> R.string.detail_error_unknown
    }
