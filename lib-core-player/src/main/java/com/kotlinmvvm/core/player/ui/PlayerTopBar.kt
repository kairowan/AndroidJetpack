package com.kotlinmvvm.core.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import com.kotlinmvvm.core.player.defaults.PlayerControlsDefaults
import com.kotlinmvvm.core.player.model.PlayerControlsStyle

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 播放器顶部标题栏，统一承载返回操作和视频标题
 */
@Composable
internal fun PlayerTopBar(
    title: String,
    onBack: (() -> Unit)?,
    icon: ImageVector,
    contentDescription: String,
    style: PlayerControlsStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(style.topBarStartColor, style.topBarEndColor)))
            .padding(PlayerControlsDefaults.TOP_BAR_PADDING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        onBack?.let { handleBack ->
            IconButton(onClick = handleBack) {
                Icon(icon, contentDescription = contentDescription, tint = style.iconColor)
            }
        }
        Text(
            text = title,
            color = style.timeTextColor,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
