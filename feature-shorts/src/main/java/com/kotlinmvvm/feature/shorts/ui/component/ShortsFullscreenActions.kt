package com.kotlinmvvm.feature.shorts.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.feature.shorts.R

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 短视频窗口模式控制组件，明确处理进入、旋转和退出全屏操作
 */
@Composable
fun ShortsFullscreenActions(
    windowMode: VideoWindowMode,
    onWindowModeChanged: (VideoWindowMode) -> Unit
) {
    val isFullscreen = windowMode != VideoWindowMode.INLINE
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isFullscreen) {
            WindowModeIconButton(
                Icons.Default.Fullscreen,
                stringResource(R.string.shorts_portrait_fullscreen)
            ) { onWindowModeChanged(VideoWindowMode.PORTRAIT_FULLSCREEN) }
            WindowModeIconButton(
                Icons.Default.ScreenRotation,
                stringResource(R.string.shorts_landscape_fullscreen)
            ) { onWindowModeChanged(VideoWindowMode.LANDSCAPE_FULLSCREEN) }
        } else {
            val target = if (windowMode == VideoWindowMode.LANDSCAPE_FULLSCREEN) {
                VideoWindowMode.PORTRAIT_FULLSCREEN
            } else {
                VideoWindowMode.LANDSCAPE_FULLSCREEN
            }
            WindowModeIconButton(
                Icons.Default.ScreenRotation,
                stringResource(
                    if (target == VideoWindowMode.PORTRAIT_FULLSCREEN) {
                        R.string.shorts_switch_portrait
                    } else {
                        R.string.shorts_switch_landscape
                    }
                )
            ) { onWindowModeChanged(target) }
            WindowModeIconButton(
                Icons.Default.FullscreenExit,
                stringResource(R.string.shorts_exit_fullscreen)
            ) { onWindowModeChanged(VideoWindowMode.INLINE) }
        }
    }
}

@Composable
private fun WindowModeIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
            .background(Color.Black.copy(alpha = 0.35f), MaterialTheme.shapes.extraLarge)
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White)
    }
}
