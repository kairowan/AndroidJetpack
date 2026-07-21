package com.kotlinmvvm.feature.detail.ui.model

import androidx.compose.ui.graphics.Color

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 详情播放器控制栏的行为与视觉配置，构造时校验所有可交互参数
 */
data class BrandedPlayerControlsConfig(
    val autoHideMs: Long = 2_600L,
    val replayMs: Long = 10_000L,
    val forwardMs: Long = 10_000L,
    val accentColor: Color = Color(0xFFFFD54F),
    val showSpeedControl: Boolean = true,
    val speedOptions: List<Float> = listOf(0.5f, 1f, 1.5f, 2f)
) {
    init {
        require(autoHideMs >= 0L) { "控制栏自动隐藏时间不能为负数" }
        require(replayMs >= 0L) { "快退时间不能为负数" }
        require(forwardMs >= 0L) { "快进时间不能为负数" }
        require(speedOptions.all { it in 0.5f..2f }) { "播放速度必须位于 0.5 到 2.0 之间" }
        require(speedOptions.zipWithNext().all { (first, second) -> first < second }) {
            "播放速度必须严格递增且不能重复"
        }
    }
}
