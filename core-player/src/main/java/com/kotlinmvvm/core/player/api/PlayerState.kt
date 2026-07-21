package com.kotlinmvvm.core.player.api

import java.util.Locale

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 暴露给 Compose 的不可变播放快照，同时区分实际输出状态和用户播放意图
 */
data class PlayerState(
    val playState: PlayState = IdlePlayState,
    val isPlaying: Boolean = false,
    val playbackRequested: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val buffered: Long = 0L,
    val speed: Float = 1f,
    val volume: Float = 1f
) {
    val progress: Float get() = progressOf(position, duration)
    val bufferedProgress: Float get() = progressOf(buffered, duration)

    fun formatPosition(): String = formatPlaybackTime(position)
    fun formatDuration(): String = formatPlaybackTime(duration)
}

private fun progressOf(value: Long, duration: Long): Float =
    if (duration > 0L) (value.toFloat() / duration).coerceIn(0f, 1f) else 0f

/** 将播放毫秒数格式化为 `m:ss` 或 `h:mm:ss`，负数按 0 处理。 */
fun formatPlaybackTime(milliseconds: Long): String {
    val totalSeconds = milliseconds.coerceAtLeast(0L) / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = totalSeconds % 3_600L / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}
