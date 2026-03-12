package com.kotlinmvvm.feature.detail

import androidx.lifecycle.ViewModel
import com.kotlinmvvm.core.data.state.VideoDetailStateHolder

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
 * @Description: 详情页 Android 状态包装 ViewModel
 */
class VideoDetailViewModel : ViewModel() {
    private val stateHolder = VideoDetailStateHolder()

    val state = stateHolder.state

    fun enterPortraitFullscreen() {
        stateHolder.enterPortraitFullscreen()
    }

    fun enterLandscapeFullscreen() {
        stateHolder.enterLandscapeFullscreen()
    }

    fun exitFullscreen() {
        stateHolder.exitFullscreen()
    }

    fun onBackPressed(): Boolean {
        return stateHolder.onBackPressed()
    }

    fun syncPlaybackSnapshot(
        positionMs: Long,
        isPlaying: Boolean,
        speed: Float
    ) {
        stateHolder.syncPlaybackSnapshot(positionMs, isPlaying, speed)
    }
}
