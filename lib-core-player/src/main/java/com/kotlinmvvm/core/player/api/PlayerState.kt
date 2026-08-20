package com.kotlinmvvm.core.player.api

/**
 * @author 浩楠
 * @date 2026/7/20
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

    private companion object {
        fun progressOf(value: Long, duration: Long): Float =
            if (duration > 0L) (value.toFloat() / duration).coerceIn(0f, 1f) else 0f
    }
}
