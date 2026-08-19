package com.kotlinmvvm.feature.detail

import com.kotlinmvvm.feature.media.FullscreenMode

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 详情页跨端状态机，平台层只负责将状态映射为窗口和播放器操作
 */
class VideoDetailStateHolder(
    initialState: VideoDetailState = VideoDetailState()
) {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<VideoDetailState> = mutableState.asStateFlow()

    fun enterPortraitFullscreen() = updateFullscreenMode(FullscreenMode.PORTRAIT)

    fun enterLandscapeFullscreen() = updateFullscreenMode(FullscreenMode.LANDSCAPE)

    fun exitFullscreen() = updateFullscreenMode(FullscreenMode.NONE)

    fun onBackPressed(): Boolean {
        val shouldPop = !mutableState.value.isFullscreen
        if (!shouldPop) exitFullscreen()
        return shouldPop
    }

    fun syncPlaybackSnapshot(
        positionMs: Long,
        isPlaying: Boolean,
        speed: Float
    ) {
        mutableState.update { current ->
            current.copy(
                playbackSnapshot = current.playbackSnapshot.copy(
                    positionMs = positionMs.takeIf { it > 0L } ?: current.playbackSnapshot.positionMs,
                    isPlaying = isPlaying,
                    speed = speed.coerceIn(MIN_PLAYBACK_SPEED, MAX_PLAYBACK_SPEED)
                )
            )
        }
    }

    private fun updateFullscreenMode(mode: FullscreenMode) {
        mutableState.update { current ->
            if (current.fullscreenMode == mode) current else current.copy(fullscreenMode = mode)
        }
    }

}

private const val MIN_PLAYBACK_SPEED = 0.5f
private const val MAX_PLAYBACK_SPEED = 2f
