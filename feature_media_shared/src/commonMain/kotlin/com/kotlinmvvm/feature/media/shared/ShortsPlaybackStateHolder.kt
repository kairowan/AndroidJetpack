package com.kotlinmvvm.feature.media.shared

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
 * 描述: Shorts 交互状态机，由各平台 ViewModel 或桥接层持有
 */
class ShortsPlaybackStateHolder(
    initialState: ShortsPlaybackState = ShortsPlaybackState()
) {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<ShortsPlaybackState> = mutableState.asStateFlow()

    fun updateCurrentPage(page: Int) {
        mutableState.update { current ->
            val normalizedPage = page.coerceAtLeast(0)
            if (current.currentPage == normalizedPage) current else current.copy(currentPage = normalizedPage)
        }
    }

    fun enterPortraitFullscreen() = updateFullscreenMode(FullscreenMode.PORTRAIT)

    fun enterLandscapeFullscreen() = updateFullscreenMode(FullscreenMode.LANDSCAPE)

    fun toggleFullscreenOrientation() {
        mutableState.update { current ->
            current.copy(
                fullscreenMode = when (current.fullscreenMode) {
                    FullscreenMode.LANDSCAPE -> FullscreenMode.PORTRAIT
                    FullscreenMode.PORTRAIT -> FullscreenMode.LANDSCAPE
                    FullscreenMode.NONE -> FullscreenMode.NONE
                }
            )
        }
    }

    fun exitFullscreen() = updateFullscreenMode(FullscreenMode.NONE)

    private fun updateFullscreenMode(mode: FullscreenMode) {
        mutableState.update { current ->
            if (current.fullscreenMode == mode) current else current.copy(fullscreenMode = mode)
        }
    }
}
