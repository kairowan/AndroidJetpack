package com.kotlinmvvm.core.player.engine

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.api.PlayState
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.cache.VideoCacheStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 基于 Media3 ExoPlayer 的实现。
 */
internal class VideoPlayer(
    context: Context,
    private val cacheStore: VideoCacheStore
) : IPlayer {

    private val appContext = context.applicationContext

    override val exoPlayer: ExoPlayer = ExoPlayer.Builder(appContext)
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheStore.buildDataSourceFactory()))
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

    override fun clearVideoOutput() { exoPlayer.clearVideoSurface() }

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
        cacheStore.preload(scope = scope, url = url, bytes = bytes)
    }

    override fun preload(urls: List<String>, bytes: Long) {
        cacheStore.preload(scope = scope, urls = urls, bytes = bytes)
    }

    override fun release() {
        scope.cancel()
        progressJob?.cancel()
        exoPlayer.release()
        cacheStore.release()
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
}
