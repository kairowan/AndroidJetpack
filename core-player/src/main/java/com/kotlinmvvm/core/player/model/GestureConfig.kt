package com.kotlinmvvm.core.player.model

import com.kotlinmvvm.core.player.defaults.PlayerGestureDefaults

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 播放器手势能力配置，控制双击、拖动方向和距离换算参数
 */
data class GestureConfig(
    val doubleTap: Boolean = true,
    val horizontalSeek: Boolean = true,
    val verticalVolume: Boolean = true,
    val verticalBrightness: Boolean = false,
    val seekMsPerPixel: Long = PlayerGestureDefaults.SEEK_MS_PER_PIXEL,
    val verticalDragDivisor: Float = PlayerGestureDefaults.VERTICAL_DRAG_DIVISOR
) {
    init {
        require(seekMsPerPixel >= 0L) { "每像素跳播时间不能为负数" }
        require(verticalDragDivisor > 0f) { "垂直拖动换算除数必须大于 0" }
    }
}
