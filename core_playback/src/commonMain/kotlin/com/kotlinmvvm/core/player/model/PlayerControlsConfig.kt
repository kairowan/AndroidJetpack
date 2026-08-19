package com.kotlinmvvm.core.player.model

import com.kotlinmvvm.core.player.defaults.PlayerBehaviorDefaults
import com.kotlinmvvm.core.player.defaults.PlayerDefaults

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
 * @Description: KMP 共享播放器控制层行为配置
 */
data class PlayerControlsConfig(
    val autoHideMs: Long = PlayerBehaviorDefaults.CONTROLS_AUTO_HIDE_MS,
    val enableAutoHide: Boolean = true,
    val rewindMs: Long = PlayerDefaults.SEEK_INTERVAL_MS,
    val forwardMs: Long = PlayerDefaults.SEEK_INTERVAL_MS,
    val showTopBar: Boolean = true,
    val showCenterControls: Boolean = true,
    val showBottomBar: Boolean = true,
    val showSpeedControl: Boolean = false,
    val speedOptions: List<Float> = emptyList()
)
