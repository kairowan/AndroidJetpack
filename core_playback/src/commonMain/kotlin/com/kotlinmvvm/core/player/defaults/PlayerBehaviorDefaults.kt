package com.kotlinmvvm.core.player.defaults

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
 * @Description: KMP 共享播放器交互默认值
 */
object PlayerBehaviorDefaults {
    const val CONTROLS_AUTO_HIDE_MS: Long = 3_000L
    const val SEEK_MS_PER_PIXEL: Long = 100L
    const val VERTICAL_DRAG_DIVISOR: Float = 500f
}
