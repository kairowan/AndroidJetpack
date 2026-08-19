package com.kotlinmvvm.core.player.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.api.PlayerState

/**
 * 收集播放器状态。
 */
@Composable
fun IPlayer.collectState(): PlayerState = state.collectAsState().value
