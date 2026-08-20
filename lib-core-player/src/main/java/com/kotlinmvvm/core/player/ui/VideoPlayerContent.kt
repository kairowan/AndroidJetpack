package com.kotlinmvvm.core.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.ext.collectState
import com.kotlinmvvm.core.player.model.GestureConfig
import com.kotlinmvvm.core.player.model.PlayerControlActions
import com.kotlinmvvm.core.player.model.PlayerControlsConfig
import com.kotlinmvvm.core.player.model.PlayerControlsIcons
import com.kotlinmvvm.core.player.model.PlayerControlsStyle

private typealias ControlsLayer = @Composable BoxScope.(VideoPlayerController, PlayerState) -> Unit
private typealias SurfaceLayer = @Composable BoxScope.(VideoPlayerController) -> Unit

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用 Compose 播放器内容容器，组合画面、手势、默认控制栏和业务扩展浮层
 */
@Composable
fun InlineVideoPlayer(
    player: VideoPlayerController,
    modifier: Modifier = Modifier,
    title: String = "",
    onBack: (() -> Unit)? = null,
    showControls: Boolean = true,
    enableGesture: Boolean = true,
    gestureConfig: GestureConfig = GestureConfig(),
    controlConfig: PlayerControlsConfig = PlayerControlsConfig(),
    controlStyle: PlayerControlsStyle = PlayerControlsStyle(),
    controlIcons: PlayerControlsIcons = PlayerControlsIcons(),
    controlActions: PlayerControlActions = PlayerControlActions(),
    surfaceContent: SurfaceLayer = { controlledPlayer ->
        PlayerSurface(player = controlledPlayer, modifier = Modifier.fillMaxSize())
    },
    controlsContent: ControlsLayer? = null,
    overlayContent: ControlsLayer = { _, _ -> },
    extraControls: @Composable RowScope.() -> Unit = {}
) {
    VideoPlayerLayout(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        player = player,
        title = title,
        onBack = onBack,
        showControls = showControls,
        enableGesture = enableGesture,
        gestureConfig = gestureConfig,
        controlConfig = controlConfig,
        controlStyle = controlStyle,
        controlIcons = controlIcons,
        controlActions = controlActions,
        surfaceContent = surfaceContent,
        controlsContent = controlsContent,
        overlayContent = overlayContent,
        extraControls = extraControls
    )
}

/** 填满父约束的播放器内容；窗口方向与系统栏仍由调用方管理。 */
@Composable
fun FullscreenVideoPlayer(
    player: VideoPlayerController,
    modifier: Modifier = Modifier,
    title: String = "",
    onBack: (() -> Unit)? = null,
    showControls: Boolean = true,
    enableGesture: Boolean = true,
    gestureConfig: GestureConfig = GestureConfig(),
    controlConfig: PlayerControlsConfig = PlayerControlsConfig(),
    controlStyle: PlayerControlsStyle = PlayerControlsStyle(),
    controlIcons: PlayerControlsIcons = PlayerControlsIcons(),
    controlActions: PlayerControlActions = PlayerControlActions(),
    surfaceContent: SurfaceLayer = { controlledPlayer ->
        PlayerSurface(player = controlledPlayer, modifier = Modifier.fillMaxSize())
    },
    controlsContent: ControlsLayer? = null,
    overlayContent: ControlsLayer = { _, _ -> },
    extraControls: @Composable RowScope.() -> Unit = {}
) {
    VideoPlayerLayout(
        modifier = modifier.fillMaxSize(),
        player = player,
        title = title,
        onBack = onBack,
        showControls = showControls,
        enableGesture = enableGesture,
        gestureConfig = gestureConfig,
        controlConfig = controlConfig,
        controlStyle = controlStyle,
        controlIcons = controlIcons,
        controlActions = controlActions,
        surfaceContent = surfaceContent,
        controlsContent = controlsContent,
        overlayContent = overlayContent,
        extraControls = extraControls
    )
}

@Composable
private fun VideoPlayerLayout(
    modifier: Modifier,
    player: VideoPlayerController,
    title: String,
    onBack: (() -> Unit)?,
    showControls: Boolean,
    enableGesture: Boolean,
    gestureConfig: GestureConfig,
    controlConfig: PlayerControlsConfig,
    controlStyle: PlayerControlsStyle,
    controlIcons: PlayerControlsIcons,
    controlActions: PlayerControlActions,
    surfaceContent: SurfaceLayer,
    controlsContent: ControlsLayer?,
    overlayContent: ControlsLayer,
    extraControls: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier.background(Color.Black)
    ) {
        surfaceContent(player)
        PlayerInteractiveOverlay(
            player = player,
            title = title,
            onBack = onBack,
            showControls = showControls,
            enableGesture = enableGesture,
            gestureConfig = gestureConfig,
            controlConfig = controlConfig,
            controlStyle = controlStyle,
            controlIcons = controlIcons,
            controlActions = controlActions,
            controlsContent = controlsContent,
            overlayContent = overlayContent,
            extraControls = extraControls
        )
    }
}

/** 将高频播放进度订阅限制在浮层重组范围内，避免反复重组 Media3 画面节点。 */
@Composable
private fun BoxScope.PlayerInteractiveOverlay(
    player: VideoPlayerController,
    title: String,
    onBack: (() -> Unit)?,
    showControls: Boolean,
    enableGesture: Boolean,
    gestureConfig: GestureConfig,
    controlConfig: PlayerControlsConfig,
    controlStyle: PlayerControlsStyle,
    controlIcons: PlayerControlsIcons,
    controlActions: PlayerControlActions,
    controlsContent: ControlsLayer?,
    overlayContent: ControlsLayer,
    extraControls: @Composable RowScope.() -> Unit
) {
    val state = player.collectState()
    val resolvedControls: ControlsLayer = controlsContent ?: { controlledPlayer, playerState ->
        PlayerControls(
            player = controlledPlayer,
            state = playerState,
            title = title,
            onBack = onBack,
            config = controlConfig,
            style = controlStyle,
            icons = controlIcons,
            actions = controlActions,
            extraControls = extraControls
        )
    }
    val contentLayer: @Composable BoxScope.() -> Unit = {
        if (showControls) resolvedControls(player, state)
        overlayContent(player, state)
    }

    if (enableGesture) {
        PlayerGestureDetector(player = player, config = gestureConfig) { contentLayer() }
    } else {
        contentLayer()
    }
}
