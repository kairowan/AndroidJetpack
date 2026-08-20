package com.kotlinmvvm.feature.detail.ui.control

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.feature.detail.R

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 品牌播放器中心控制区，提供快退、播放暂停和快进操作
 */
@Composable
fun BrandedPlayerCenterControls(
    state: PlayerState,
    accentColor: Color,
    onReplay: () -> Unit,
    onToggle: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        IconButton(onClick = onReplay, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Replay10, stringResource(R.string.detail_rewind), tint = Color.White, modifier = Modifier.size(34.dp))
        }
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(68.dp).background(
                Brush.radialGradient(listOf(accentColor, accentColor.copy(alpha = 0.65f))),
                CircleShape
            )
        ) {
            val isPlayRequested = state.playbackRequested
            Icon(
                if (isPlayRequested) Icons.Default.Pause else Icons.Default.PlayArrow,
                stringResource(if (isPlayRequested) R.string.detail_pause else R.string.detail_play),
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }
        IconButton(onClick = onForward, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Forward10, stringResource(R.string.detail_forward), tint = Color.White, modifier = Modifier.size(34.dp))
        }
    }
}
