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
 * 描述: 竖向短视频分页的预加载数量、浮层边距和渐变透明度默认值
 */
object ShortVideoPagerDefaults {
    const val PRELOAD_COUNT: Int = 2
    val OVERLAY_PADDING: Dp = 16.dp
    const val OVERLAY_GRADIENT_ALPHA: Float = 0.7f
}
