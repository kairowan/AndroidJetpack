package com.kotlinmvvm.feature.detail

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.kotlinmvvm.core.data.state.VideoDetailState
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.categoryDurationLabel
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.ext.collectState
import com.kotlinmvvm.core.player.feature.ResumePlaybackFeature
import com.kotlinmvvm.core.player.provider.rememberPlayer
import com.kotlinmvvm.core.player.ui.FullscreenVideoPlayer
import com.kotlinmvvm.core.player.ui.VideoPlayerView
import com.kotlinmvvm.feature.detail.model.BrandedPlayerControlsConfig
import com.kotlinmvvm.feature.media.shared.VideoDetailPagePresenter

/**
 * @author 浩楠
 *
 * @date 2026-2-28
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: 详情页播放器与详情信息展示界面
 */

@Composable
fun VideoDetailScreen(
    video: EyepetizerFeedItem.Video,
    state: VideoDetailState,
    onBack: () -> Unit,
    onEnterPortraitFullscreen: () -> Unit,
    onEnterLandscapeFullscreen: () -> Unit,
    onExitFullscreen: () -> Unit,
    onSavePlaybackSnapshot: (positionMs: Long, isPlaying: Boolean, speed: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val player = rememberPlayer()
    val playerState = player.collectState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val latestState = rememberUpdatedState(state)
    val latestSavePlaybackSnapshot = rememberUpdatedState(onSavePlaybackSnapshot)
    val fullscreenMode = state.fullscreenMode
    val isFullscreen = state.isFullscreen
    val pageModel = remember(video, state) {
        VideoDetailPagePresenter.present(
            video = video,
            state = state
        )
    }
    val controlsConfig = remember {
        BrandedPlayerControlsConfig()
    }
    val resumeFeature = remember(video.id) {
        ResumePlaybackFeature(
            readPosition = { latestState.value.playbackSnapshot.positionMs },
            readIsPlaying = { latestState.value.playbackSnapshot.isPlaying },
            readSpeed = { latestState.value.playbackSnapshot.speed },
            onSave = { positionMs, isPlaying, speed ->
                latestSavePlaybackSnapshot.value(positionMs, isPlaying, speed)
            }
        )
    }
    val checkpointSecond = playerState.position / 1000

    // 实时同步保存播放快照，避免横竖屏切换时因生命周期先后导致进度丢失。
    LaunchedEffect(checkpointSecond, playerState.isPlaying, playerState.speed) {
        onSavePlaybackSnapshot(playerState.position, playerState.isPlaying, playerState.speed)
    }

    LaunchedEffect(activity, fullscreenMode) {
        activity?.applyVideoWindowMode(fullscreenMode)
    }

    DisposableEffect(activity) {
        onDispose {
            activity?.applyVideoWindowMode(VideoDetailState.FullscreenMode.NONE)
        }
    }

    BackHandler(enabled = isFullscreen) {
        onBack()
    }

    val customControls: @Composable BoxScope.(IPlayer, PlayerState) -> Unit = { controlledPlayer, state ->
        BrandedPlayerControls(
            player = controlledPlayer,
            state = state,
            title = video.title,
            onBack = onBack,
            isFullscreen = isFullscreen,
            isLandscapeFullscreen = fullscreenMode == VideoDetailState.FullscreenMode.LANDSCAPE,
            onEnterPortraitFullscreen = onEnterPortraitFullscreen,
            onEnterLandscapeFullscreen = onEnterLandscapeFullscreen,
            onExitFullscreen = onExitFullscreen,
            config = controlsConfig
        )
    }

    if (isFullscreen) {
        FullscreenVideoPlayer(
            url = pageModel.playUrl,
            player = player,
            title = pageModel.title,
            onBack = onBack,
            controlsContent = customControls,
            features = listOf(resumeFeature)
        )
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            VideoPlayerView(
                url = pageModel.playUrl,
                player = player,
                title = pageModel.title,
                onBack = onBack,
                controlsContent = customControls,
                features = listOf(resumeFeature)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = pageModel.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = pageModel.metadataLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = pageModel.authorIcon,
                        contentDescription = pageModel.authorName,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Gray, shape = MaterialTheme.shapes.small)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = pageModel.authorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (pageModel.descriptionText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = pageModel.descriptionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun Activity.applyVideoWindowMode(mode: VideoDetailState.FullscreenMode) {
    val targetOrientation = when (mode) {
        VideoDetailState.FullscreenMode.NONE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        VideoDetailState.FullscreenMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        VideoDetailState.FullscreenMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
    if (requestedOrientation != targetOrientation) {
        requestedOrientation = targetOrientation
    }

    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    if (mode == VideoDetailState.FullscreenMode.NONE) {
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    } else {
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
