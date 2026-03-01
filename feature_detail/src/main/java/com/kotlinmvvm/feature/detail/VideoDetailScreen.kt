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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.player.FullscreenVideoPlayer
import com.kotlinmvvm.core.player.IPlayer
import com.kotlinmvvm.core.player.PlayerFeature
import com.kotlinmvvm.core.player.PlayerState
import com.kotlinmvvm.core.player.VideoPlayerView
import com.kotlinmvvm.core.player.collectState
import com.kotlinmvvm.core.player.rememberPlayer

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
 * @Description: TODO
 */

private enum class VideoFullscreenMode {
    NONE,
    PORTRAIT,
    LANDSCAPE
}

@Composable
fun VideoDetailScreen(
    video: EyepetizerFeedItem.Video,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = rememberPlayer()
    val playerState = player.collectState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var fullscreenModeName by rememberSaveable { mutableStateOf(VideoFullscreenMode.NONE.name) }
    val fullscreenMode = remember(fullscreenModeName) { VideoFullscreenMode.valueOf(fullscreenModeName) }
    val isFullscreen = fullscreenMode != VideoFullscreenMode.NONE
    val controlsConfig = remember {
        BrandedPlayerControlsConfig(
            autoHideMs = 2600L,
            replayMs = 8_000L,
            forwardMs = 12_000L
        )
    }
    var savedPositionMs by rememberSaveable(video.id) { mutableLongStateOf(0L) }
    var savedIsPlaying by rememberSaveable(video.id) { mutableStateOf(true) }
    var savedSpeed by rememberSaveable(video.id) { mutableFloatStateOf(1f) }
    val resumeFeature = remember(video.id) {
        ResumePlaybackFeature(
            readPosition = { savedPositionMs },
            readIsPlaying = { savedIsPlaying },
            readSpeed = { savedSpeed },
            onSave = { positionMs, isPlaying, speed ->
                savedPositionMs = positionMs
                savedIsPlaying = isPlaying
                savedSpeed = speed
            }
        )
    }
    val checkpointSecond = playerState.position / 1000

    // 实时同步保存播放快照，避免横竖屏切换时因生命周期先后导致进度丢失。
    LaunchedEffect(checkpointSecond, playerState.isPlaying, playerState.speed) {
        if (playerState.position > 0L) {
            savedPositionMs = playerState.position
        }
        savedIsPlaying = playerState.isPlaying
        savedSpeed = playerState.speed
    }

    fun updateFullscreenMode(mode: VideoFullscreenMode) {
        fullscreenModeName = mode.name
    }

    LaunchedEffect(activity, fullscreenMode) {
        activity?.applyVideoWindowMode(fullscreenMode)
    }

    DisposableEffect(activity) {
        onDispose {
            activity?.applyVideoWindowMode(VideoFullscreenMode.NONE)
        }
    }

    BackHandler(enabled = isFullscreen) {
        updateFullscreenMode(VideoFullscreenMode.NONE)
    }

    val handleBack = {
        if (isFullscreen) {
            updateFullscreenMode(VideoFullscreenMode.NONE)
        } else {
            onBack()
        }
    }

    val customControls: @Composable BoxScope.(IPlayer, PlayerState) -> Unit = { controlledPlayer, state ->
        BrandedPlayerControls(
            player = controlledPlayer,
            state = state,
            title = video.title,
            onBack = handleBack,
            isFullscreen = isFullscreen,
            isLandscapeFullscreen = fullscreenMode == VideoFullscreenMode.LANDSCAPE,
            onEnterPortraitFullscreen = { updateFullscreenMode(VideoFullscreenMode.PORTRAIT) },
            onEnterLandscapeFullscreen = { updateFullscreenMode(VideoFullscreenMode.LANDSCAPE) },
            onExitFullscreen = { updateFullscreenMode(VideoFullscreenMode.NONE) },
            config = controlsConfig
        )
    }

    if (isFullscreen) {
        FullscreenVideoPlayer(
            url = video.playUrl,
            player = player,
            title = video.title,
            onBack = handleBack,
            controlsContent = customControls,
            features = listOf(resumeFeature)
        )
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            VideoPlayerView(
                url = video.playUrl,
                player = player,
                title = video.title,
                onBack = handleBack,
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
                    text = video.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "#${video.category} · ${formatDuration(video.duration)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = video.authorIcon,
                        contentDescription = video.authorName,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Gray, shape = MaterialTheme.shapes.small)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = video.authorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (video.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = video.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private class ResumePlaybackFeature(
    private val readPosition: () -> Long,
    private val readIsPlaying: () -> Boolean,
    private val readSpeed: () -> Float,
    private val onSave: (positionMs: Long, isPlaying: Boolean, speed: Float) -> Unit
) : PlayerFeature {

    private var appliedForUrl: String? = null

    override fun onUrlChanged(player: IPlayer, url: String, autoPlay: Boolean) {
        if (appliedForUrl == url) return
        appliedForUrl = url

        val savedPosition = readPosition()
        if (savedPosition > 0L) {
            player.seekTo(savedPosition)
        }

        val savedSpeed = readSpeed()
        if (savedSpeed > 0f) {
            player.setSpeed(savedSpeed)
        }

        if (!readIsPlaying()) {
            player.pause()
        }
    }

    override fun onDetach(player: IPlayer) {
        val snapshot = player.state.value
        onSave(
            snapshot.position.coerceAtLeast(0L),
            snapshot.isPlaying,
            snapshot.speed
        )
        appliedForUrl = null
    }
}

private fun Activity.applyVideoWindowMode(mode: VideoFullscreenMode) {
    val targetOrientation = when (mode) {
        VideoFullscreenMode.NONE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        VideoFullscreenMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        VideoFullscreenMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
    if (requestedOrientation != targetOrientation) {
        requestedOrientation = targetOrientation
    }

    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    if (mode == VideoFullscreenMode.NONE) {
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

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
