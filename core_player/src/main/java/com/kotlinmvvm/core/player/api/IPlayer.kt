package com.kotlinmvvm.core.player.api

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.kotlinmvvm.core.player.defaults.PlayerDefaults
import kotlinx.coroutines.flow.StateFlow

/**
 * 播放器能力接口。
 */
interface IPlayer {
    val state: StateFlow<PlayerState>
    val exoPlayer: ExoPlayer

    fun play(url: String)
    fun play(mediaItem: MediaItem)
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
