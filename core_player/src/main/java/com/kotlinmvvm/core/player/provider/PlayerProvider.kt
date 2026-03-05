package com.kotlinmvvm.core.player.provider

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.facade.PlayerFactory
import com.kotlinmvvm.core.player.facade.PlayerLifecycleBinder

/**
 * 当前组合树中的播放器实例。
 */
val LocalPlayer = staticCompositionLocalOf<IPlayer> {
    error("No Player provided")
}

/**
 * 创建并绑定生命周期的播放器实例。
 */
@Composable
fun rememberPlayer(): IPlayer {
    val appContext = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val player = rememberPlayerInstance(appContext)

    BindPlayerLifecycle(player = player, lifecycle = lifecycle)

    return player
}

@Composable
private fun rememberPlayerInstance(appContext: Context): IPlayer {
    return remember(appContext) {
        PlayerFactory.create(appContext)
    }
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

/**
 * 播放器作用域。
 */
@Composable
fun PlayerProvider(
    player: IPlayer = rememberPlayer(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPlayer provides player) {
        content()
    }
}

/**
 * 获取当前播放器。
 */
@Composable
fun currentPlayer(): IPlayer = LocalPlayer.current
