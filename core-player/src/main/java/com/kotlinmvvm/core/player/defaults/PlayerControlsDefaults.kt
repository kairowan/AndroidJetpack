package com.kotlinmvvm.core.player.defaults

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用播放器控制栏的尺寸、间距、时长和透明度默认值
 */
object PlayerControlsDefaults {
    const val AUTO_HIDE_MS: Long = 3_000L
    val CENTER_BUTTON_SPACING: Dp = 24.dp
    val SIDE_BUTTON_SIZE: Dp = 48.dp
    val SIDE_ICON_SIZE: Dp = 36.dp
    val PLAY_BUTTON_SIZE: Dp = 64.dp
    val PLAY_ICON_SIZE: Dp = 40.dp
    val TOP_BAR_PADDING: Dp = 8.dp
    val BOTTOM_BAR_HORIZONTAL_PADDING: Dp = 16.dp
    val BOTTOM_BAR_VERTICAL_PADDING: Dp = 8.dp
    val PROGRESS_SLIDER_HEIGHT: Dp = 26.dp
    val PROGRESS_TRACK_HEIGHT: Dp = 4.dp
    const val TOP_BAR_GRADIENT_START_ALPHA: Float = 0.7f
    const val BOTTOM_BAR_GRADIENT_END_ALPHA: Float = 0.72f
    const val PROGRESS_BUFFERED_ALPHA: Float = 0.45f
    const val PROGRESS_UNPLAYED_ALPHA: Float = 0.2f
    const val CENTER_PLAY_BUTTON_ALPHA: Float = 0.5f
}
