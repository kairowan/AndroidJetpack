package com.kotlinmvvm.core.player.api

import com.kotlinmvvm.core.player.defaults.PlayerDefaults
import kotlinx.coroutines.flow.StateFlow

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
 * @Description: KMP 共享播放器控制协议
 */
interface PlaybackController {
    val state: StateFlow<PlayerState>

    fun play(url: String)
    fun pause()
    fun resume()
    fun toggle()
    fun stop()
    fun clearVideoOutput()
    fun release()

    fun seekTo(ms: Long)
    fun seekTo(progress: Float)
    fun forward(ms: Long = PlayerDefaults.SEEK_INTERVAL_MS)
    fun rewind(ms: Long = PlayerDefaults.SEEK_INTERVAL_MS)

    fun setSpeed(speed: Float)
    fun setVolume(volume: Float)

    fun preload(url: String, bytes: Long = PlayerDefaults.PRELOAD_BYTES)
    fun preload(urls: List<String>, bytes: Long = PlayerDefaults.PRELOAD_BYTES)
}
