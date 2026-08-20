package com.kotlinmvvm.feature.detail.ui.control

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.feature.detail.R

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 品牌播放器顶部控制栏，负责返回、标题、全屏方向切换和扩展操作槽位
 */
@Composable
fun BrandedPlayerTopBar(
    title: String,
    onBack: (() -> Unit)?,
    windowMode: VideoWindowMode,
    onWindowModeChanged: (VideoWindowMode) -> Unit,
    endActions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    val isFullscreen = windowMode != VideoWindowMode.INLINE
    Row(
        modifier = modifier.fillMaxWidth().background(
            Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent))
        ).padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.detail_back),
                        tint = Color.White
                    )
                }
            }
            Text(
                title,
                color = Color.White,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (!isFullscreen) {
                WindowModeButton(Icons.Default.Fullscreen, R.string.detail_portrait_fullscreen) {
                    onWindowModeChanged(VideoWindowMode.PORTRAIT_FULLSCREEN)
                }
                WindowModeButton(Icons.Default.ScreenRotation, R.string.detail_landscape_fullscreen) {
                    onWindowModeChanged(VideoWindowMode.LANDSCAPE_FULLSCREEN)
                }
            } else {
                val target = if (windowMode == VideoWindowMode.LANDSCAPE_FULLSCREEN) {
                    VideoWindowMode.PORTRAIT_FULLSCREEN
                } else VideoWindowMode.LANDSCAPE_FULLSCREEN
                WindowModeButton(
                    Icons.Default.ScreenRotation,
                    if (target == VideoWindowMode.PORTRAIT_FULLSCREEN) {
                        R.string.detail_switch_portrait
                    } else R.string.detail_switch_landscape
                ) { onWindowModeChanged(target) }
                WindowModeButton(Icons.Default.FullscreenExit, R.string.detail_exit_fullscreen) {
                    onWindowModeChanged(VideoWindowMode.INLINE)
                }
            }
            endActions()
        }
    }
}

@Composable
private fun WindowModeButton(icon: ImageVector, contentDescriptionRes: Int, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(34.dp)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
    ) {
        Icon(icon, contentDescription = stringResource(contentDescriptionRes), tint = Color.White)
    }
}
