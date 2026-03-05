package com.kotlinmvvm.core.player.api

/**
 * 播放状态。
 */
sealed interface PlayState {
    data object Idle : PlayState
    data object Buffering : PlayState
    data object Ready : PlayState
    data object Ended : PlayState
    data class Error(val message: String?) : PlayState
}
