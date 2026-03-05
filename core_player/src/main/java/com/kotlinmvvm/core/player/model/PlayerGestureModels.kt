package com.kotlinmvvm.core.player.model

import com.kotlinmvvm.core.player.defaults.PlayerGestureDefaults

/**
 * 播放手势配置参数。
 */
data class GestureConfig(
    val doubleTap: Boolean = true,
    val horizontalSeek: Boolean = true,
    val verticalVolume: Boolean = true,
    val verticalBrightness: Boolean = true,
    val seekMsPerPixel: Long = PlayerGestureDefaults.SEEK_MS_PER_PIXEL,
    val verticalDragDivisor: Float = PlayerGestureDefaults.VERTICAL_DRAG_DIVISOR
)
