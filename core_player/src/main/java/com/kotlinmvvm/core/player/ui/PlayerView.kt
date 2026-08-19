package com.kotlinmvvm.core.player.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.media3.common.util.UnstableApi
import com.kotlinmvvm.core.player.api.IPlayer
import androidx.media3.ui.compose.ContentFrame

/**
 * @author 浩楠
 *
 * @date 2026/7/24 12:18
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Android Media3 播放画面与常用尺寸包装
 */

/**
 * 播放器视图 - 纯视频渲染，不含控制器
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerSurface(
    player: IPlayer,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    ContentFrame(
        player = player.media3Player,
        modifier = modifier,
        contentScale = contentScale,
        shutter = {
            Box(Modifier.fillMaxSize().background(Color.Black))
        }
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
