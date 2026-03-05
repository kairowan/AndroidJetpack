package com.kotlinmvvm.core.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.ext.collectState
import com.kotlinmvvm.core.player.feature.PlayerFeature
import com.kotlinmvvm.core.player.model.GestureConfig
import com.kotlinmvvm.core.player.model.PlayerControlActions
import com.kotlinmvvm.core.player.model.PlayerControlsConfig
import com.kotlinmvvm.core.player.model.PlayerControlsIcons
import com.kotlinmvvm.core.player.model.PlayerControlsStyle
import com.kotlinmvvm.core.player.provider.rememberPlayer

private typealias ControlsLayer = @Composable BoxScope.(IPlayer, PlayerState) -> Unit
private typealias SurfaceLayer = @Composable BoxScope.(IPlayer) -> Unit

/**
 * 16:9 视频播放器。
 */
@Composable
fun VideoPlayerView(
    url: String,
    modifier: Modifier = Modifier,
    player: IPlayer = rememberPlayer(),
    title: String = "",
    onBack: (() -> Unit)? = null,
    autoPlay: Boolean = true,
    showControls: Boolean = true,
    enableGesture: Boolean = true,
    gestureConfig: GestureConfig = GestureConfig(),
    controlConfig: PlayerControlsConfig = PlayerControlsConfig(),
    controlStyle: PlayerControlsStyle = PlayerControlsStyle(),
    controlIcons: PlayerControlsIcons = PlayerControlsIcons(),
    controlActions: PlayerControlActions = PlayerControlActions(),
    features: List<PlayerFeature> = emptyList(),
    surfaceContent: SurfaceLayer = { controlledPlayer ->
        PlayerSurface(player = controlledPlayer, modifier = Modifier.fillMaxSize())
    },
    controlsContent: ControlsLayer? = null,
    overlayContent: ControlsLayer = { _, _ -> },
    extraControls: @Composable RowScope.() -> Unit = {}
) {
    BaseVideoPlayerView(
        url = url,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        player = player,
        title = title,
        onBack = onBack,
        autoPlay = autoPlay,
        showControls = showControls,
        enableGesture = enableGesture,
        gestureConfig = gestureConfig,
        controlConfig = controlConfig,
        controlStyle = controlStyle,
        controlIcons = controlIcons,
        controlActions = controlActions,
        features = features,
        surfaceContent = surfaceContent,
        controlsContent = controlsContent,
        overlayContent = overlayContent,
        extraControls = extraControls
    )
}

/**
 * 全屏播放器。
 */
@Composable
fun FullscreenVideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    player: IPlayer = rememberPlayer(),
    title: String = "",
    onBack: (() -> Unit)? = null,
    autoPlay: Boolean = true,
    showControls: Boolean = true,
    enableGesture: Boolean = true,
    controlConfig: PlayerControlsConfig = PlayerControlsConfig(),
    controlStyle: PlayerControlsStyle = PlayerControlsStyle(),
    controlIcons: PlayerControlsIcons = PlayerControlsIcons(),
    controlActions: PlayerControlActions = PlayerControlActions(),
    features: List<PlayerFeature> = emptyList(),
    surfaceContent: SurfaceLayer = { controlledPlayer ->
        PlayerSurfaceFullscreen(player = controlledPlayer)
    },
    controlsContent: ControlsLayer? = null,
    overlayContent: ControlsLayer = { _, _ -> },
    extraControls: @Composable RowScope.() -> Unit = {}
) {
    BaseVideoPlayerView(
        url = url,
        modifier = modifier.fillMaxSize(),
        player = player,
        title = title,
        onBack = onBack,
        autoPlay = autoPlay,
        showControls = showControls,
        enableGesture = enableGesture,
        gestureConfig = GestureConfig(),
        controlConfig = controlConfig,
        controlStyle = controlStyle,
        controlIcons = controlIcons,
        controlActions = controlActions,
        features = features,
        surfaceContent = surfaceContent,
        controlsContent = controlsContent,
        overlayContent = overlayContent,
        extraControls = extraControls
    )
}

@Composable
private fun BaseVideoPlayerView(
    url: String,
    modifier: Modifier,
    player: IPlayer,
    title: String,
    onBack: (() -> Unit)?,
    autoPlay: Boolean,
    showControls: Boolean,
    enableGesture: Boolean,
    gestureConfig: GestureConfig,
    controlConfig: PlayerControlsConfig,
    controlStyle: PlayerControlsStyle,
    controlIcons: PlayerControlsIcons,
    controlActions: PlayerControlActions,
    features: List<PlayerFeature>,
    surfaceContent: SurfaceLayer,
    controlsContent: ControlsLayer?,
    overlayContent: ControlsLayer,
    extraControls: @Composable RowScope.() -> Unit
) {
    val state = player.collectState()

    ObserveFeatures(player = player, features = features)
    ObserveUrlPlayback(
        player = player,
        url = url,
        autoPlay = autoPlay,
        features = features
    )

    val resolvedControls: ControlsLayer = controlsContent ?: { controlledPlayer, _ ->
        PlayerControls(
            player = controlledPlayer,
            title = title,
            onBack = onBack,
            config = controlConfig,
            style = controlStyle,
            icons = controlIcons,
            actions = controlActions,
            extraControls = extraControls
        )
    }

    Box(
        modifier = modifier.background(Color.Black)
    ) {
        surfaceContent(player)

        val contentLayer: @Composable BoxScope.() -> Unit = {
            if (showControls) {
                resolvedControls(player, state)
            }
            overlayContent(player, state)
        }

        if (enableGesture) {
            PlayerGestureDetector(
                player = player,
                config = gestureConfig
            ) {
                contentLayer()
            }
        } else {
            contentLayer()
        }
    }
}

@Composable
private fun ObserveFeatures(
    player: IPlayer,
    features: List<PlayerFeature>
) {
    DisposableEffect(player, features) {
        features.forEach { feature -> feature.onAttach(player) }
        onDispose {
            features.forEach { feature -> feature.onDetach(player) }
        }
    }
}

@Composable
private fun ObserveUrlPlayback(
    player: IPlayer,
    url: String,
    autoPlay: Boolean,
    features: List<PlayerFeature>
) {
    LaunchedEffect(player, url, autoPlay, features) {
        if (autoPlay) {
            player.play(url)
        }
        features.forEach { feature ->
            feature.onUrlChanged(player, url, autoPlay)
        }
    }
}

/**
 * 简单播放器 - 无控制层。
 */
@Composable
fun SimplePlayer(
    url: String,
    player: IPlayer,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    LaunchedEffect(url, autoPlay) {
        if (autoPlay) {
            player.play(url)
        }
    }

    PlayerSurfaceFullscreen(player = player, modifier = modifier)
}
