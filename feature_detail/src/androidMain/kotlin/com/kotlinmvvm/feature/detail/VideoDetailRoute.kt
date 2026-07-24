package com.kotlinmvvm.feature.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.core.player.ext.collectState
import com.kotlinmvvm.core.player.feature.ResumePlaybackFeature
import com.kotlinmvvm.core.player.model.PlayerControlsStyle
import com.kotlinmvvm.core.player.preset.VideoDetailPlaybackPreset
import com.kotlinmvvm.core.player.provider.rememberPlayer
import com.kotlinmvvm.core.player.ui.FullscreenVideoPlayer
import com.kotlinmvvm.core.player.ui.VideoPlayerView
import com.kotlinmvvm.core.ui.base.viewModelFactory
import com.kotlinmvvm.feature.media.shared.FullscreenMode
import com.kotlinmvvm.feature.media.shared.VideoDetailPagePresenter
import com.kotlinmvvm.feature.media.shared.applyVideoWindowMode
import com.kotlinmvvm.feature.media.shared.findActivity

/**
 * @author 浩楠
 * @date 2026/7/24 15:02
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情 Android 入口，只负责 ViewModel、Media3、Coil 与窗口能力注入
 */
@Composable
fun VideoDetailRoute(
    video: EyepetizerFeedItem.Video,
    videoPlayerFactory: VideoPlayerFactory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    providedViewModel: VideoDetailViewModel? = null
) {
    val viewModel = providedViewModel ?: viewModel(
        factory = viewModelFactory { VideoDetailViewModel() }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pageModel = remember(video, state) {
        VideoDetailPagePresenter.present(video, state)
    }
    val player = rememberPlayer(videoPlayerFactory)
    val playerState = player.collectState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val latestState by rememberUpdatedState(state)
    val resumeFeature = remember(video.id) {
        ResumePlaybackFeature(
            readPosition = { latestState.playbackSnapshot.positionMs },
            readIsPlaying = { latestState.playbackSnapshot.isPlaying },
            readSpeed = { latestState.playbackSnapshot.speed },
            onSave = viewModel::syncPlaybackSnapshot
        )
    }
    val playerFeatures = remember(resumeFeature) { listOf(resumeFeature) }
    val controlsStyle = remember {
        PlayerControlsStyle(
            progressPlayedColor = DetailAccentColor,
            centerPlayButtonColor = DetailAccentColor
        )
    }
    val handleBack = {
        if (viewModel.onBackPressed()) onBack()
    }

    LaunchedEffect(playerState.position / 1_000, playerState.isPlaying, playerState.speed) {
        viewModel.syncPlaybackSnapshot(
            playerState.position,
            playerState.isPlaying,
            playerState.speed
        )
    }

    LaunchedEffect(activity, state.fullscreenMode) {
        activity?.applyVideoWindowMode(state.fullscreenMode)
    }

    DisposableEffect(activity) {
        onDispose {
            activity?.applyVideoWindowMode(FullscreenMode.NONE)
        }
    }

    BackHandler(enabled = state.isFullscreen, onBack = handleBack)

    VideoDetailScreen(
        pageModel = pageModel,
        player = { isFullscreen, playerModifier ->
            val extraControls: @Composable RowScope.() -> Unit = {
                VideoDetailFullscreenActions(
                    pageModel = pageModel,
                    onEnterPortraitFullscreen = viewModel::enterPortraitFullscreen,
                    onEnterLandscapeFullscreen = viewModel::enterLandscapeFullscreen,
                    onExitFullscreen = viewModel::exitFullscreen
                )
            }
            if (isFullscreen) {
                FullscreenVideoPlayer(
                    url = pageModel.playUrl,
                    player = player,
                    title = pageModel.title,
                    onBack = handleBack,
                    controlConfig = VideoDetailPlaybackPreset.controlsConfig,
                    controlStyle = controlsStyle,
                    features = playerFeatures,
                    extraControls = extraControls,
                    modifier = playerModifier
                )
            } else {
                VideoPlayerView(
                    url = pageModel.playUrl,
                    player = player,
                    title = pageModel.title,
                    onBack = handleBack,
                    controlConfig = VideoDetailPlaybackPreset.controlsConfig,
                    controlStyle = controlsStyle,
                    features = playerFeatures,
                    extraControls = extraControls,
                    modifier = playerModifier
                )
            }
        },
        authorImage = { url, contentDescription, imageModifier ->
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = imageModifier
            )
        },
        modifier = modifier
    )
}

private val DetailAccentColor = Color(0xFFFFC107)
