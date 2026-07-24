package com.kotlinmvvm.core.player.api

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

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
 * 描述: Android 播放器接口，仅向 UI 暴露稳定的 Media3 Player 契约
 */
interface IPlayer : PlaybackController {
    val media3Player: Player

    fun play(mediaItem: MediaItem)
}
