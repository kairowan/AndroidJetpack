package com.kotlinmvvm.core.player.model

import com.kotlinmvvm.core.player.api.VideoPlayerController

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用播放器控制栏命令映射，允许业务层替换默认播放操作而不改动组件
 */
data class PlayerControlActions(
    val onRewind: (VideoPlayerController, Long) -> Unit = { player, ms -> player.rewind(ms) },
    val onToggle: (VideoPlayerController) -> Unit = VideoPlayerController::toggle,
    val onForward: (VideoPlayerController, Long) -> Unit = { player, ms -> player.forward(ms) },
    val onSeekProgress: (VideoPlayerController, Float) -> Unit = VideoPlayerController::seekTo
)
