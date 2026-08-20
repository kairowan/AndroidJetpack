package com.kotlinmvvm.core.player.engine

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DataSource
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.api.PlayState
import com.kotlinmvvm.core.player.api.ErrorPlayState
import com.kotlinmvvm.core.player.api.EndedPlayState
import com.kotlinmvvm.core.player.api.ReadyPlayState
import com.kotlinmvvm.core.player.api.BufferingPlayState
import com.kotlinmvvm.core.player.api.IdlePlayState
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.cache.VideoCacheStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Media3 播放控制器实现，在主线程维护引擎、播放意图、进度状态和视频预加载
 */
@OptIn(UnstableApi::class)
internal class Media3VideoPlayerController(
    context: Context,
    private val upstreamFactory: DataSource.Factory
) : VideoPlayerController, PlayerLifecycleController {

    private val appContext = context.applicationContext

    private val exoPlayer = ExoPlayer.Builder(appContext)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                VideoCacheStore.buildDataSourceFactory(appContext, upstreamFactory)
            )
        )
        .build()
    override val media3Player: Player
        get() {
            ensureActive()
            return exoPlayer
        }

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val progressTicker = PlayerProgressTicker(scope, ::updateState)
    private val preloadCoordinator = VideoPreloadCoordinator(scope, appContext, upstreamFactory)
    private var currentUrl: String? = null
    private var playbackRequested = false
    private var released = false

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) playbackRequested = false
                updateState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
                if (isPlaying) progressTicker.start() else progressTicker.stop()
            }

            override fun onPlayerError(error: PlaybackException) {
                playbackRequested = false
                progressTicker.stop()
                _state.update {
                    it.copy(
                        playState = ErrorPlayState(error.toPlayerFailure()),
                        isPlaying = false,
                        playbackRequested = false
                    )
                }
            }
        })
    }

    override fun play(url: String, autoPlay: Boolean) {
        ensureActive()
        require(url.isNotBlank()) { "视频地址不能为空" }
        playbackRequested = autoPlay

        val mediaChanged = currentUrl != url
        if (mediaChanged) {
            currentUrl = url
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
        }
        when {
            mediaChanged || exoPlayer.playbackState == Player.STATE_IDLE -> exoPlayer.prepare()
            exoPlayer.playbackState == Player.STATE_ENDED -> exoPlayer.seekTo(0L)
        }
        exoPlayer.playWhenReady = playbackRequested
        updateState()
    }

    override fun pause() {
        ensureActive()
        playbackRequested = false
        exoPlayer.playWhenReady = false
        updateState()
    }

    override fun resume() {
        ensureActive()
        playbackRequested = true
        when (exoPlayer.playbackState) {
            Player.STATE_ENDED -> exoPlayer.seekTo(0L)
            Player.STATE_IDLE -> if (exoPlayer.mediaItemCount > 0) exoPlayer.prepare()
            Player.STATE_BUFFERING, Player.STATE_READY -> Unit
        }
        exoPlayer.playWhenReady = true
        updateState()
    }

    override fun toggle() {
        ensureActive()
        if (playbackRequested) pause() else resume()
    }

    override fun stop() {
        ensureActive()
        playbackRequested = false
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        currentUrl = null
        progressTicker.stop()
        updateState()
    }

    override fun seekTo(ms: Long) {
        ensureActive()
        val target = ms.coerceAtLeast(0L)
        val duration = exoPlayer.duration
        exoPlayer.seekTo(if (duration > 0L) target.coerceAtMost(duration) else target)
        updateState()
    }

    override fun seekTo(progress: Float) {
        ensureActive()
        val duration = exoPlayer.duration
        if (duration > 0L) seekTo((duration * progress.coerceIn(0f, 1f)).toLong())
    }

    override fun forward(ms: Long) {
        ensureActive()
        seekTo(exoPlayer.currentPosition + ms)
    }

    override fun rewind(ms: Long) {
        ensureActive()
        seekTo(exoPlayer.currentPosition - ms)
    }

    override fun setSpeed(speed: Float) {
        ensureActive()
        exoPlayer.setPlaybackSpeed(speed.coerceIn(0.5f, 2f))
        updateState()
    }

    override fun setVolume(volume: Float) {
        ensureActive()
        exoPlayer.volume = volume.coerceIn(0f, 1f)
        updateState()
    }

    override fun preload(url: String, bytes: Long) {
        ensureActive()
        preloadCoordinator.preload(url, bytes)
    }

    override fun preload(urls: List<String>, bytes: Long) {
        ensureActive()
        preloadCoordinator.replace(urls, bytes)
    }

    /** 生命周期退到后台时只暂停传输，不覆盖用户原本的播放意图。 */
    override fun pauseForLifecycle(): Boolean {
        ensureActive()
        val shouldResume = playbackRequested
        exoPlayer.playWhenReady = false
        updateState()
        return shouldResume
    }

    /** 仅在进入后台前仍有播放意图时恢复传输。 */
    override fun resumeAfterLifecycle() {
        ensureActive()
        if (!playbackRequested) return
        exoPlayer.playWhenReady = true
        updateState()
    }

    override fun release() {
        ensurePlayerMainThread()
        if (released) return
        released = true
        preloadCoordinator.cancel()
        scope.cancel()
        progressTicker.stop()
        exoPlayer.release()
    }

    private fun updateState() {
        _state.value = PlayerState(
            playState = exoPlayer.playerError?.let { ErrorPlayState(it.toPlayerFailure()) }
                ?: when (exoPlayer.playbackState) {
                    Player.STATE_IDLE -> IdlePlayState
                    Player.STATE_BUFFERING -> BufferingPlayState
                    Player.STATE_READY -> ReadyPlayState
                    Player.STATE_ENDED -> EndedPlayState
                    else -> IdlePlayState
            },
            isPlaying = exoPlayer.isPlaying,
            playbackRequested = playbackRequested,
            position = exoPlayer.currentPosition.coerceAtLeast(0),
            duration = exoPlayer.duration.coerceAtLeast(0),
            buffered = exoPlayer.bufferedPosition.coerceAtLeast(0),
            speed = exoPlayer.playbackParameters.speed,
            volume = exoPlayer.volume
        )
    }

    private fun ensureActive() {
        ensurePlayerMainThread()
        check(!released) { "播放器已经释放" }
    }
}
