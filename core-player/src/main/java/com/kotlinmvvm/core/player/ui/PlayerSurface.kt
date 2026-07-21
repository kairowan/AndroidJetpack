package com.kotlinmvvm.core.player.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import com.kotlinmvvm.core.player.api.VideoPlayerController

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 基于 Media3 Compose ContentFrame 的纯视频画面层，不包含业务控制组件
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerSurface(
    player: VideoPlayerController,
    modifier: Modifier = Modifier
) {
    ContentFrame(
        player = player.media3Player,
        modifier = modifier.background(Color.Black)
    )
}
