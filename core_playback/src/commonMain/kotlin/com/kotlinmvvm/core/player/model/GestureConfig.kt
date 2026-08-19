package com.kotlinmvvm.core.player.model

import com.kotlinmvvm.core.player.defaults.PlayerBehaviorDefaults

/**
 * @author 浩楠
 *
 * @date 2026-3-11
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: KMP 共享播放器手势配置参数
 */
data class GestureConfig(
    val doubleTap: Boolean = true,
    val horizontalSeek: Boolean = true,
    val verticalVolume: Boolean = true,
    val verticalBrightness: Boolean = true,
    val seekMsPerPixel: Long = PlayerBehaviorDefaults.SEEK_MS_PER_PIXEL,
    val verticalDragDivisor: Float = PlayerBehaviorDefaults.VERTICAL_DRAG_DIVISOR
)
