package com.kotlinmvvm.feature.detail.model

import androidx.compose.ui.graphics.Color
import com.kotlinmvvm.core.player.model.PlayerControlsConfig
import com.kotlinmvvm.core.player.preset.VideoDetailPlaybackPreset

/**
 * @author 浩楠
 *
 * @date 2026-3-9
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: 详情页品牌化控制层配置
 */
data class BrandedPlayerControlsConfig(
    val controlsConfig: PlayerControlsConfig = VideoDetailPlaybackPreset.controlsConfig,
    val accentColor: Color = Color(0xFFFFC107),
)
