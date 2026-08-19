package com.kotlinmvvm.feature.detail

import androidx.lifecycle.ViewModel

/**
 * @author 浩楠
 * @date 2026/7/24 12:10
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 详情页 Android 生命周期包装，所有状态变更委托给共享状态机
 */
class VideoDetailViewModel : ViewModel() {
    private val stateHolder = VideoDetailStateHolder()

    val state = stateHolder.state

    fun enterPortraitFullscreen() = stateHolder.enterPortraitFullscreen()

    fun enterLandscapeFullscreen() = stateHolder.enterLandscapeFullscreen()

    fun exitFullscreen() = stateHolder.exitFullscreen()

    fun onBackPressed(): Boolean = stateHolder.onBackPressed()

    fun syncPlaybackSnapshot(
        positionMs: Long,
        isPlaying: Boolean,
        speed: Float
    ) = stateHolder.syncPlaybackSnapshot(positionMs, isPlaying, speed)
}
