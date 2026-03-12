package com.kotlinmvvm.core.data.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
 * @Description: 详情页共享状态持有器
 */
class VideoDetailStateHolder(
    initialState: VideoDetailState = VideoDetailState()
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<VideoDetailState> = _state.asStateFlow()

    fun enterPortraitFullscreen() {
        updateFullscreenMode(VideoDetailState.FullscreenMode.PORTRAIT)
    }

    fun enterLandscapeFullscreen() {
        updateFullscreenMode(VideoDetailState.FullscreenMode.LANDSCAPE)
    }

    fun exitFullscreen() {
        updateFullscreenMode(VideoDetailState.FullscreenMode.NONE)
    }

    fun onBackPressed(): Boolean {
        val shouldPop = !_state.value.isFullscreen
        if (!shouldPop) {
            exitFullscreen()
        }
        return shouldPop
    }

    fun syncPlaybackSnapshot(
        positionMs: Long,
        isPlaying: Boolean,
        speed: Float
    ) {
        _state.update { current ->
            current.copy(
                playbackSnapshot = current.playbackSnapshot.copy(
                    positionMs = if (positionMs > 0L) {
                        positionMs
                    } else {
                        current.playbackSnapshot.positionMs
                    },
                    isPlaying = isPlaying,
                    speed = speed
                )
            )
        }
    }

    private fun updateFullscreenMode(mode: VideoDetailState.FullscreenMode) {
        _state.update { current ->
            if (current.fullscreenMode == mode) {
                current
            } else {
                current.copy(fullscreenMode = mode)
            }
        }
    }
}
