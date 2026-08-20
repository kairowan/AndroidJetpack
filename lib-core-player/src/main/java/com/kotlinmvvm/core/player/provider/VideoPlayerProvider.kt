package com.kotlinmvvm.core.player.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.core.player.engine.PlayerLifecycleController

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Compose 播放器提供器，使控制器跟随当前组合和 Lifecycle 自动暂停、恢复与释放
 */
@Composable
fun rememberVideoPlayerController(factory: VideoPlayerFactory): VideoPlayerController {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val player = remember(factory) { factory.create() }

    DisposableEffect(player, lifecycle) {
        var resumeOnForeground = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    resumeOnForeground = (player as? PlayerLifecycleController)
                        ?.pauseForLifecycle()
                        ?: player.state.value.playbackRequested.also { player.pause() }
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (resumeOnForeground) {
                        (player as? PlayerLifecycleController)?.resumeAfterLifecycle()
                            ?: player.resume()
                    }
                    resumeOnForeground = false
                }

                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    return player
}
