package com.kotlinmvvm.core.player.api

/**
 * 播放器状态。
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
    val progress: Float get() = if (duration > 0) position.toFloat() / duration else 0f
    val bufferedProgress: Float get() = if (duration > 0) buffered.toFloat() / duration else 0f

    fun formatPosition(): String = formatMs(position)
    fun formatDuration(): String = formatMs(duration)
}

private fun formatMs(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val remainSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainSeconds)
}
