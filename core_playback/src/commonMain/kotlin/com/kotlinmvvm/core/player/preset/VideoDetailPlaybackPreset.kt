package com.kotlinmvvm.core.player.preset

import com.kotlinmvvm.core.player.model.PlayerControlsConfig

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
 * @Description: 详情页播放器共享行为预设
 */
object VideoDetailPlaybackPreset {
    const val AUTO_HIDE_MS: Long = 2_600L
    const val REWIND_MS: Long = 8_000L
    const val FORWARD_MS: Long = 12_000L
    val SPEED_OPTIONS: List<Float> = listOf(1f, 1.25f, 1.5f, 2f)

    val controlsConfig: PlayerControlsConfig = PlayerControlsConfig(
        autoHideMs = AUTO_HIDE_MS,
        rewindMs = REWIND_MS,
        forwardMs = FORWARD_MS,
        showSpeedControl = true,
        speedOptions = SPEED_OPTIONS
    )
}
