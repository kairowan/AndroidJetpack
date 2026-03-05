package com.kotlinmvvm.core.player.defaults

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 播放能力默认值。
 */
object PlayerDefaults {
    const val SEEK_INTERVAL_MS: Long = 10_000L
    const val PRELOAD_BYTES: Long = 2L * 1024 * 1024
}

/**
 * 控制层默认值。
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

    val TOP_BAR_GRADIENT_START_ALPHA: Float = 0.7f
    val BOTTOM_BAR_GRADIENT_END_ALPHA: Float = 0.72f
    val PROGRESS_BUFFERED_ALPHA: Float = 0.45f
    val PROGRESS_UNPLAYED_ALPHA: Float = 0.2f
    val CENTER_PLAY_BUTTON_ALPHA: Float = 0.5f
}

/**
 * 手势默认值。
 */
object PlayerGestureDefaults {
    const val SEEK_MS_PER_PIXEL: Long = 100L
    const val VERTICAL_DRAG_DIVISOR: Float = 500f
}

/**
 * 短视频默认值。
 */
object ShortsPagerDefaults {
    const val PRELOAD_COUNT: Int = 2
    val OVERLAY_PADDING: Dp = 16.dp
    val OVERLAY_GRADIENT_ALPHA: Float = 0.7f
}

@Deprecated(
    message = "Use PlayerDefaults.SEEK_INTERVAL_MS",
    replaceWith = ReplaceWith("PlayerDefaults.SEEK_INTERVAL_MS")
)
const val DEFAULT_SEEK_INTERVAL_MS: Long = PlayerDefaults.SEEK_INTERVAL_MS

@Deprecated(
    message = "Use PlayerDefaults.PRELOAD_BYTES",
    replaceWith = ReplaceWith("PlayerDefaults.PRELOAD_BYTES")
)
const val DEFAULT_PRELOAD_BYTES: Long = PlayerDefaults.PRELOAD_BYTES
