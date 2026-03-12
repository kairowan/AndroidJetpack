package com.kotlinmvvm.core.player.state

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
 * @Description: KMP 共享播放器倍速切换规则
 */
object PlaybackSpeedStepper {
    fun nextSpeed(current: Float, candidates: List<Float>): Float {
        if (candidates.isEmpty()) return current
        val index = candidates.indexOfFirst { value -> value >= current }.coerceAtLeast(0)
        return if (index == candidates.lastIndex) candidates.first() else candidates[index + 1]
    }
}
