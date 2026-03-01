package com.kotlinmvvm.core.player

import android.content.Context
import androidx.compose.runtime.*
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * @author 浩楠
 *
 * @date 2026-2-25
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

/**
 * 播放状态
 */
sealed interface PlayState {
    object Idle : PlayState
    object Buffering : PlayState
    object Ready : PlayState
    object Ended : PlayState
    data class Error(val message: String?) : PlayState
}

/**
 * 播放器状态
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
    
    fun formatPosition(): String = formatTime(position)
    fun formatDuration(): String = formatTime(duration)
}

private fun formatTime(ms: Long): String {
    val seconds = ms / 1000
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

/**
 * 播放器接口
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
    fun release()
    
    fun seekTo(ms: Long)
    fun seekTo(progress: Float)
    fun forward(ms: Long = 10_000)
    fun rewind(ms: Long = 10_000)
    
    fun setSpeed(speed: Float)
    fun setVolume(volume: Float)

    fun preload(url: String, bytes: Long = DEFAULT_PRELOAD_BYTES)
    fun preload(urls: List<String>, bytes: Long = DEFAULT_PRELOAD_BYTES)
}

/**
 * 播放器实现
 */
class VideoPlayer private constructor(
    context: Context
) : IPlayer {

    private val appContext = context.applicationContext

    override val exoPlayer: ExoPlayer = ExoPlayer.Builder(appContext)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                VideoCacheManager.buildCacheDataSourceFactory(appContext)
            )
        )
        .build()
    
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var currentUrl: String? = null
    
    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) = updateState()
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
                if (isPlaying) startProgress() else stopProgress()
            }
            override fun onPlayerError(error: PlaybackException) {
                _state.value = PlayerState(playState = PlayState.Error(error.message))
            }
        })
    }
    
    private fun startProgress() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                updateState()
                delay(250)
            }
        }
    }
    
    private fun stopProgress() {
        progressJob?.cancel()
    }
    
    private fun updateState() {
        _state.value = PlayerState(
            playState = when (exoPlayer.playbackState) {
                Player.STATE_IDLE -> PlayState.Idle
                Player.STATE_BUFFERING -> PlayState.Buffering
                Player.STATE_READY -> PlayState.Ready
                Player.STATE_ENDED -> PlayState.Ended
                else -> PlayState.Idle
            },
            isPlaying = exoPlayer.isPlaying,
            position = exoPlayer.currentPosition.coerceAtLeast(0),
            duration = exoPlayer.duration.coerceAtLeast(0),
            buffered = exoPlayer.bufferedPosition,
            speed = exoPlayer.playbackParameters.speed,
            volume = exoPlayer.volume
        )
    }
    
    override fun play(url: String) {
        if (currentUrl != url) {
            currentUrl = url
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
        }
        exoPlayer.playWhenReady = true
    }
    
    override fun play(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }
    
    override fun pause() { exoPlayer.playWhenReady = false }
    override fun resume() { exoPlayer.playWhenReady = true }
    override fun toggle() { exoPlayer.playWhenReady = !exoPlayer.isPlaying }
    override fun stop() { exoPlayer.stop(); currentUrl = null }
    
    override fun seekTo(ms: Long) {
        exoPlayer.seekTo(ms.coerceIn(0, exoPlayer.duration.coerceAtLeast(0)))
    }
    
    override fun seekTo(progress: Float) {
        seekTo((exoPlayer.duration * progress.coerceIn(0f, 1f)).toLong())
    }
    
    override fun forward(ms: Long) = seekTo(exoPlayer.currentPosition + ms)
    override fun rewind(ms: Long) = seekTo(exoPlayer.currentPosition - ms)
    
    override fun setSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed.coerceIn(0.5f, 2f))
    }
    
    override fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f)
    }

    override fun preload(url: String, bytes: Long) {
        VideoCacheManager.preload(
            scope = scope,
            context = appContext,
            url = url,
            bytes = bytes
        )
    }

    override fun preload(urls: List<String>, bytes: Long) {
        VideoCacheManager.preload(
            scope = scope,
            context = appContext,
            urls = urls,
            bytes = bytes
        )
    }
    
    override fun release() {
        scope.cancel()
        progressJob?.cancel()
        exoPlayer.release()
    }
    
    companion object {
        fun create(context: Context): VideoPlayer = VideoPlayer(context)
    }
}
