package com.kotlinmvvm.core.player.xml

import android.content.Context
import android.graphics.Color as AndroidColor
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.lifecycle.Lifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.kotlinmvvm.core.player.R
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.facade.PlayerFactory
import com.kotlinmvvm.core.player.facade.PlayerLifecycleBinder
import com.kotlinmvvm.core.player.facade.PlayerLifecycleBinding

/**
 * 可在 XML 中直接使用的视频播放器中间层。
 */
@OptIn(UnstableApi::class)
class VideoPlayerHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val playerView: PlayerView
    private var player: IPlayer? = null
    private var lifecycleBinding: PlayerLifecycleBinding? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.core_player_view_video_player, this, true)
        playerView = findViewById(R.id.playerView)
        playerView.useController = false
        playerView.setShutterBackgroundColor(AndroidColor.TRANSPARENT)
    }

    fun attachPlayer(player: IPlayer) {
        if (this.player === player) return
        this.player = player
        playerView.player = player.exoPlayer
    }

    fun createAndAttachPlayer(): IPlayer {
        val created = PlayerFactory.create(context)
        attachPlayer(created)
        return created
    }

    fun bindLifecycle(
        lifecycle: Lifecycle,
        releaseOnDestroy: Boolean = true
    ) {
        val boundPlayer = ensurePlayer()
        lifecycleBinding?.unbind()
        lifecycleBinding = PlayerLifecycleBinder.bind(
            lifecycle = lifecycle,
            player = boundPlayer,
            releaseOnDestroy = releaseOnDestroy
        )
    }

    fun play(url: String) {
        ensurePlayer().play(url)
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        player?.resume()
    }

    fun stop() {
        player?.stop()
    }

    fun setUseController(enabled: Boolean) {
        playerView.useController = enabled
    }

    fun setResizeMode(resizeMode: Int) {
        playerView.resizeMode = resizeMode
    }

    fun currentPlayer(): IPlayer? = player

    fun detachPlayer(releasePlayer: Boolean = false) {
        lifecycleBinding?.unbind()
        lifecycleBinding = null
        playerView.player = null
        if (releasePlayer) {
            player?.release()
        }
        player = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        playerView.player = null
    }

    private fun ensurePlayer(): IPlayer {
        return player ?: createAndAttachPlayer()
    }
}
