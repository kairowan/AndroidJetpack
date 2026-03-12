package com.kotlinmvvm.core.data.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
 * @Description: Shorts 播放交互的共享状态持有器
 */
class ShortsPlaybackStateHolder(
    initialState: ShortsPlaybackState = ShortsPlaybackState()
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<ShortsPlaybackState> = _state.asStateFlow()

    fun updateCurrentPage(page: Int) {
        _state.update { current ->
            val normalizedPage = page.coerceAtLeast(0)
            if (current.currentPage == normalizedPage) {
                current
            } else {
                current.copy(currentPage = normalizedPage)
            }
        }
    }

    fun enterPortraitFullscreen() {
        updateFullscreenMode(ShortsPlaybackState.FullscreenMode.PORTRAIT)
    }

    fun enterLandscapeFullscreen() {
        updateFullscreenMode(ShortsPlaybackState.FullscreenMode.LANDSCAPE)
    }

    fun toggleFullscreenOrientation() {
        _state.update { current ->
            current.copy(
                fullscreenMode = when (current.fullscreenMode) {
                    ShortsPlaybackState.FullscreenMode.LANDSCAPE -> ShortsPlaybackState.FullscreenMode.PORTRAIT
                    ShortsPlaybackState.FullscreenMode.PORTRAIT -> ShortsPlaybackState.FullscreenMode.LANDSCAPE
                    ShortsPlaybackState.FullscreenMode.NONE -> ShortsPlaybackState.FullscreenMode.NONE
                }
            )
        }
    }

    fun exitFullscreen() {
        updateFullscreenMode(ShortsPlaybackState.FullscreenMode.NONE)
    }

    private fun updateFullscreenMode(mode: ShortsPlaybackState.FullscreenMode) {
        _state.update { current ->
            if (current.fullscreenMode == mode) {
                current
            } else {
                current.copy(fullscreenMode = mode)
            }
        }
    }
}
