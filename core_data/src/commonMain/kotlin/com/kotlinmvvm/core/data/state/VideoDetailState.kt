package com.kotlinmvvm.core.data.state

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
 * @Description: 详情页共享状态，包含全屏模式与播放快照
 */
data class VideoDetailState(
    val fullscreenMode: FullscreenMode = FullscreenMode.NONE,
    val playbackSnapshot: PlaybackSnapshot = PlaybackSnapshot()
) {
    val isFullscreen: Boolean
        get() = fullscreenMode != FullscreenMode.NONE

    enum class FullscreenMode {
        NONE,
        PORTRAIT,
        LANDSCAPE
    }

    data class PlaybackSnapshot(
        val positionMs: Long = 0L,
        val isPlaying: Boolean = true,
        val speed: Float = 1f
    )
}
