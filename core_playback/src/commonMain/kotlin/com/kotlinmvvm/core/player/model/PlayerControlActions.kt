package com.kotlinmvvm.core.player.model

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
 * @Description: KMP 共享播放器控制层操作回调
 */
data class PlayerControlActions(
    val onRewind: (PlaybackController, Long) -> Unit = { player, ms -> player.rewind(ms) },
    val onToggle: (PlaybackController) -> Unit = { player -> player.toggle() },
    val onForward: (PlaybackController, Long) -> Unit = { player, ms -> player.forward(ms) },
    val onSeekProgress: (PlaybackController, Float) -> Unit = { player, progress -> player.seekTo(progress) }
)
