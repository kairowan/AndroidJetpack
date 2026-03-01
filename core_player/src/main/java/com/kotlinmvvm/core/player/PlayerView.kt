package com.kotlinmvvm.core.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * 播放器视图 - 纯视频渲染，不含控制器
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerSurface(
    player: IPlayer,
    modifier: Modifier = Modifier,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT
) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player.exoPlayer
                useController = false
                this.resizeMode = resizeMode
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            view.player = player.exoPlayer
        },
        modifier = modifier.background(Color.Black)
    )
}

/**
 * 16:9 播放器
 */
@Composable
fun PlayerSurface16x9(
    player: IPlayer,
    modifier: Modifier = Modifier
) {
    PlayerSurface(
        player = player,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    )
}

/**
 * 全屏播放器
 */
@Composable
fun PlayerSurfaceFullscreen(
    player: IPlayer,
    modifier: Modifier = Modifier
) {
    PlayerSurface(
        player = player,
        modifier = modifier.fillMaxSize()
    )
}
