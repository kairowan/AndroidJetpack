package com.kotlinmvvm.core.player.feature

import com.kotlinmvvm.core.player.api.PlaybackController

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
 * @Description: KMP 共享续播功能，负责恢复和保存播放快照
 */
class ResumePlaybackFeature(
    private val readPosition: () -> Long,
    private val readIsPlaying: () -> Boolean,
    private val readSpeed: () -> Float,
    private val onSave: (positionMs: Long, isPlaying: Boolean, speed: Float) -> Unit
) : PlayerFeature {

    override fun onDetach(player: PlaybackController) {
        save(player)
    }

    override fun onUrlChanged(player: PlaybackController, url: String, autoPlay: Boolean) {
        val position = readPosition().coerceAtLeast(0L)
        if (position > 0L) {
            player.seekTo(position)
        }

        player.setSpeed(readSpeed())

        if (!readIsPlaying()) {
            player.pause()
        } else if (autoPlay) {
            player.resume()
        }
    }

    private fun save(player: PlaybackController) {
        val snapshot = player.state.value
        onSave(snapshot.position, snapshot.isPlaying, snapshot.speed)
    }
}
