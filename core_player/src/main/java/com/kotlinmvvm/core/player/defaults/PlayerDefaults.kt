package com.kotlinmvvm.core.player.defaults

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 控制层默认值。
 */
object PlayerControlsDefaults {
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

    val TOP_BAR_GRADIENT_START_ALPHA: Float = 0.7f
    val BOTTOM_BAR_GRADIENT_END_ALPHA: Float = 0.72f
    val PROGRESS_BUFFERED_ALPHA: Float = 0.45f
    val PROGRESS_UNPLAYED_ALPHA: Float = 0.2f
    val CENTER_PLAY_BUTTON_ALPHA: Float = 0.5f
}

/**
 * 短视频默认值。
 */
object ShortsPagerDefaults {
    val OVERLAY_PADDING: Dp = 16.dp
    val OVERLAY_GRADIENT_ALPHA: Float = 0.7f
}
