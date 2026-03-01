package com.kotlinmvvm.core.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 功能插件：可扩展播放器行为与覆盖层 UI。
 */
interface PlayerFeature {
    fun onAttach(player: IPlayer) {}
    fun onDetach(player: IPlayer) {}
    fun onUrlChanged(player: IPlayer, url: String, autoPlay: Boolean) {}
}

/**
 * 完整的视频播放器组件 - 组合 Surface + Controls + Gesture
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
    surfaceContent: @Composable BoxScope.(IPlayer) -> Unit = { p ->
        PlayerSurface(player = p, modifier = Modifier.fillMaxSize())
    },
    controlsContent: (@Composable BoxScope.(IPlayer, PlayerState) -> Unit)? = null,
    overlayContent: @Composable BoxScope.(IPlayer, PlayerState) -> Unit = { _, _ -> },
    extraControls: @Composable RowScope.() -> Unit = {}
) {
    val state = player.collectState()

    DisposableEffect(player, features) {
        features.forEach { it.onAttach(player) }
        onDispose {
            features.forEach { it.onDetach(player) }
        }
    }

    LaunchedEffect(url) {
        if (autoPlay) player.play(url)
        features.forEach { it.onUrlChanged(player, url, autoPlay) }
    }

    val resolvedControls: @Composable BoxScope.(IPlayer, PlayerState) -> Unit = controlsContent ?: { p, _ ->
        PlayerControls(
            player = p,
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
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        // 视频渲染层（可替换）
        surfaceContent(player)
        
        // 手势层
        if (enableGesture) {
            PlayerGestureDetector(
                player = player,
                config = gestureConfig
            ) {
                // 控制层
                if (showControls) {
                    resolvedControls(player, state)
                }
                overlayContent(player, state)
            }
        } else if (showControls) {
            resolvedControls(player, state)
            overlayContent(player, state)
        } else {
            overlayContent(player, state)
        }
    }
}

/**
 * 全屏视频播放器
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
    surfaceContent: @Composable BoxScope.(IPlayer) -> Unit = { p ->
        PlayerSurfaceFullscreen(player = p)
    },
    controlsContent: (@Composable BoxScope.(IPlayer, PlayerState) -> Unit)? = null,
    overlayContent: @Composable BoxScope.(IPlayer, PlayerState) -> Unit = { _, _ -> },
    extraControls: @Composable RowScope.() -> Unit = {}
) {
    val state = player.collectState()

    DisposableEffect(player, features) {
        features.forEach { it.onAttach(player) }
        onDispose {
            features.forEach { it.onDetach(player) }
        }
    }

    LaunchedEffect(url) {
        if (autoPlay) player.play(url)
        features.forEach { it.onUrlChanged(player, url, autoPlay) }
    }

    val resolvedControls: @Composable BoxScope.(IPlayer, PlayerState) -> Unit = controlsContent ?: { p, _ ->
        PlayerControls(
            player = p,
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
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        surfaceContent(player)
        
        if (enableGesture) {
            PlayerGestureDetector(player = player) {
                if (showControls) {
                    resolvedControls(player, state)
                }
                overlayContent(player, state)
            }
        } else if (showControls) {
            resolvedControls(player, state)
            overlayContent(player, state)
        } else {
            overlayContent(player, state)
        }
    }
}

/**
 * 简单播放器 - 无控制UI，用于短视频
 */
@Composable
fun SimplePlayer(
    url: String,
    player: IPlayer,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    LaunchedEffect(url) {
        if (autoPlay) player.play(url)
    }
    
    PlayerSurfaceFullscreen(player = player, modifier = modifier)
}
