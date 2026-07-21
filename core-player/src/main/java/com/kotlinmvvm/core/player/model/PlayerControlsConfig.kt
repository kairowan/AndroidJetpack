package com.kotlinmvvm.core.player.model

import com.kotlinmvvm.core.player.defaults.PlayerControlsDefaults
import com.kotlinmvvm.core.player.defaults.PlayerDefaults

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用播放器控制栏行为配置，控制显隐区域、自动隐藏和跳播步长
 */
data class PlayerControlsConfig(
    val autoHideMs: Long = PlayerControlsDefaults.AUTO_HIDE_MS,
    val enableAutoHide: Boolean = true,
    val rewindMs: Long = PlayerDefaults.SEEK_INTERVAL_MS,
    val forwardMs: Long = PlayerDefaults.SEEK_INTERVAL_MS,
    val showTopBar: Boolean = true,
    val showCenterControls: Boolean = true,
    val showBottomBar: Boolean = true
) {
    init {
        require(autoHideMs >= 0L) { "控制栏自动隐藏时间不能为负数" }
        require(rewindMs >= 0L) { "快退时间不能为负数" }
        require(forwardMs >= 0L) { "快进时间不能为负数" }
    }
}
