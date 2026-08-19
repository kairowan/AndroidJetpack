package com.kotlinmvvm.core.player.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.core.player.facade.PlayerLifecycleBinder

/**
 * 使用应用提供的工厂创建播放器，并把实例所有权绑定到当前组合。
 */
@Composable
fun rememberPlayer(factory: VideoPlayerFactory): IPlayer {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val player = remember(factory) { factory.create() }

    BindPlayerLifecycle(player = player, lifecycle = lifecycle)

    return player
}

@Composable
private fun BindPlayerLifecycle(
    player: IPlayer,
    lifecycle: Lifecycle
) {
    DisposableEffect(player, lifecycle) {
        val binding = PlayerLifecycleBinder.bind(
            lifecycle = lifecycle,
            player = player,
            releaseOnDestroy = false
        )
        onDispose {
            binding.unbind()
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
}
