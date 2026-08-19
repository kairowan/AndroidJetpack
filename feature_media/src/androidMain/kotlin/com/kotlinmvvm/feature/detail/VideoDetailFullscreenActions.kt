package com.kotlinmvvm.feature.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * @author 浩楠
 * @date 2026/7/24 15:02
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 复用 core-player 控制层时追加的详情页全屏操作
 */
@Composable
internal fun VideoDetailFullscreenActions(
    pageModel: VideoDetailPageModel,
    onEnterPortraitFullscreen: () -> Unit,
    onEnterLandscapeFullscreen: () -> Unit,
    onExitFullscreen: () -> Unit
) {
    if (pageModel.isFullscreen) {
        IconButton(
            onClick = if (pageModel.isLandscapeFullscreen) {
                onEnterPortraitFullscreen
            } else {
                onEnterLandscapeFullscreen
            }
        ) {
            Icon(
                imageVector = Icons.Default.ScreenRotation,
                contentDescription = pageModel.controlsCopy.toggleOrientationLabel,
                tint = Color.White
            )
        }
        IconButton(onClick = onExitFullscreen) {
            Icon(
                imageVector = Icons.Default.FullscreenExit,
                contentDescription = pageModel.controlsCopy.exitFullscreenLabel,
                tint = Color.White
            )
        }
    } else {
        IconButton(onClick = onEnterPortraitFullscreen) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = pageModel.controlsCopy.enterPortraitFullscreenLabel,
                tint = Color.White
            )
        }
        IconButton(onClick = onEnterLandscapeFullscreen) {
            Icon(
                imageVector = Icons.Default.ScreenRotation,
                contentDescription = pageModel.controlsCopy.enterLandscapeFullscreenLabel,
                tint = Color.White
            )
        }
    }
}
