package com.kotlinmvvm.core.player.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用播放器控制栏图标集合，支持业务品牌替换默认 Material 图标
 */
data class PlayerControlsIcons(
    val back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    val rewind: ImageVector = Icons.Default.Replay10,
    val play: ImageVector = Icons.Default.PlayArrow,
    val pause: ImageVector = Icons.Default.Pause,
    val forward: ImageVector = Icons.Default.Forward10
)
