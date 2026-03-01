package com.kotlinmvvm.core.player

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * @author 浩楠
 *
 * @date 2026-2-26
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

/**
 * 播放器 Provider - 提供播放器实例和生命周期管理
 */
val LocalPlayer = staticCompositionLocalOf<IPlayer> { 
    error("No Player provided") 
}

/**
 * 创建并记住播放器实例
 */
@Composable
fun rememberPlayer(): IPlayer {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    
    val player = remember { VideoPlayer.create(context) }
    
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> player.pause()
                Lifecycle.Event.ON_RESUME -> if (player.state.value.playState == PlayState.Ready) player.resume()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            player.release()
        }
    }
    
    return player
}

/**
 * 播放器作用域
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
 * 获取当前播放器
 */
@Composable
fun currentPlayer(): IPlayer = LocalPlayer.current

/**
 * 收集播放器状态
 */
@Composable
fun IPlayer.collectState(): PlayerState {
    return state.collectAsState().value
}
