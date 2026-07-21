package com.kotlinmvvm.core.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.defaults.PlayerControlsDefaults
import com.kotlinmvvm.core.player.model.PlayerControlsStyle

/**
 * @author 浩楠
 * @date 2026/7/20 10:09
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 播放器底部控制区，统一显示进度、时长和业务扩展按钮
 */
@Composable
internal fun PlayerBottomControls(
    state: PlayerState,
    style: PlayerControlsStyle,
    onSeek: (Float) -> Unit,
    extraControls: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(style.bottomBarStartColor, style.bottomBarEndColor)))
            .padding(
                horizontal = PlayerControlsDefaults.BOTTOM_BAR_HORIZONTAL_PADDING,
                vertical = PlayerControlsDefaults.BOTTOM_BAR_VERTICAL_PADDING
            )
    ) {
        PlayerProgressBar(
            value = state.progress,
            bufferedValue = state.bufferedProgress,
            onValueChange = onSeek,
            style = style
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.formatPosition()} / ${state.formatDuration()}",
                color = style.timeTextColor,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(PlayerControlsDefaults.BOTTOM_BAR_VERTICAL_PADDING),
                verticalAlignment = Alignment.CenterVertically,
                content = extraControls
            )
        }
    }
}

@Composable
private fun PlayerProgressBar(
    value: Float,
    bufferedValue: Float,
    onValueChange: (Float) -> Unit,
    style: PlayerControlsStyle
) {
    val played = value.coerceIn(0f, 1f)
    val buffered = bufferedValue.coerceIn(0f, 1f)
    val trackShape = RoundedCornerShape(percent = 50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PlayerControlsDefaults.PROGRESS_SLIDER_HEIGHT),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(style.progressTrackHeight)
                .background(style.progressUnplayedColor, trackShape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(buffered)
                .height(style.progressTrackHeight)
                .background(style.progressBufferedColor, trackShape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(played)
                .height(style.progressTrackHeight)
                .background(style.progressPlayedColor, trackShape)
        )

        Slider(
            value = played,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = style.progressPlayedColor,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}
