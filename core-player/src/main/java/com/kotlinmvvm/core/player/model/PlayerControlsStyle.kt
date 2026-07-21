package com.kotlinmvvm.core.player.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.kotlinmvvm.core.player.defaults.PlayerControlsDefaults

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用播放器控制栏视觉样式，允许业务模块替换颜色和进度轨道尺寸
 */
data class PlayerControlsStyle(
    val topBarStartColor: Color = Color.Black.copy(alpha = PlayerControlsDefaults.TOP_BAR_GRADIENT_START_ALPHA),
    val topBarEndColor: Color = Color.Transparent,
    val bottomBarStartColor: Color = Color.Transparent,
    val bottomBarEndColor: Color = Color.Black.copy(alpha = PlayerControlsDefaults.BOTTOM_BAR_GRADIENT_END_ALPHA),
    val iconColor: Color = Color.White,
    val timeTextColor: Color = Color.White,
    val progressPlayedColor: Color = Color.White,
    val progressBufferedColor: Color = Color.White.copy(alpha = PlayerControlsDefaults.PROGRESS_BUFFERED_ALPHA),
    val progressUnplayedColor: Color = Color.White.copy(alpha = PlayerControlsDefaults.PROGRESS_UNPLAYED_ALPHA),
    val centerPlayButtonColor: Color = Color.Black.copy(alpha = PlayerControlsDefaults.CENTER_PLAY_BUTTON_ALPHA),
    val progressTrackHeight: Dp = PlayerControlsDefaults.PROGRESS_TRACK_HEIGHT
)
