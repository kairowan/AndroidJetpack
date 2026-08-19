package com.kotlinmvvm.core.player.api

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
 * @Description: KMP 共享播放器状态
 */
data class PlayerState(
    val playState: PlayState = PlayState.Idle,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val buffered: Long = 0L,
    val speed: Float = 1f,
    val volume: Float = 1f
) {
    val progress: Float
        get() = if (duration > 0L) position.toFloat() / duration else 0f

    val bufferedProgress: Float
        get() = if (duration > 0L) buffered.toFloat() / duration else 0f

    fun formatPosition(): String = formatMs(position)

    fun formatDuration(): String = formatMs(duration)
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    val paddedSeconds = if (seconds < 10L) "0$seconds" else seconds.toString()
    return "$minutes:$paddedSeconds"
}
