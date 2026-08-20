package com.kotlinmvvm.feature.detail.ui.control

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.feature.detail.R
import com.kotlinmvvm.feature.detail.ui.model.BrandedPlayerControlsConfig

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 品牌播放器底部控制区，展示进度、缓冲、播放时间并处理拖动和倍速切换
 */
@Composable
fun BrandedPlayerBottomControls(
    state: PlayerState,
    config: BrandedPlayerControlsConfig,
    onSeek: (Float) -> Unit,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().background(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)))
        ).padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        BrandedProgressBar(state.progress, state.bufferedProgress, config.accentColor, onSeek)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${state.formatPosition()} / ${state.formatDuration()}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            if (config.showSpeedControl && config.speedOptions.isNotEmpty()) {
                AssistChip(
                    onClick = { onSpeedSelected(nextSpeed(state.speed, config.speedOptions)) },
                    label = {
                        Text(
                            stringResource(R.string.detail_speed, state.speed),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = config.accentColor.copy(alpha = 0.28f),
                        labelColor = Color.White
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
private fun BrandedProgressBar(
    progress: Float,
    bufferedProgress: Float,
    accentColor: Color,
    onSeek: (Float) -> Unit
) {
    val played = progress.coerceIn(0f, 1f)
    val buffered = bufferedProgress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(percent = 50)
    Box(Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.CenterStart) {
        Box(Modifier.fillMaxWidth().height(5.dp).background(Color.White.copy(alpha = 0.2f), shape))
        Box(Modifier.fillMaxWidth(buffered).height(5.dp).background(Color.White.copy(alpha = 0.5f), shape))
        Box(Modifier.fillMaxWidth(played).height(5.dp).background(accentColor, shape))
        Slider(
            value = played,
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

internal fun nextSpeed(current: Float, candidates: List<Float>): Float {
    if (candidates.isEmpty()) return current
    val index = candidates.indexOfFirst { it >= current }.coerceAtLeast(0)
    return if (index == candidates.lastIndex) candidates.first() else candidates[index + 1]
}
