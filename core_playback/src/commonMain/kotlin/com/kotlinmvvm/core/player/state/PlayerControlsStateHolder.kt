package com.kotlinmvvm.core.player.state

import com.kotlinmvvm.core.player.model.PlayerControlsConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
 * @Description: KMP 共享播放器控制层交互状态持有器
 */
class PlayerControlsStateHolder(
    initialState: PlayerControlsUiState = PlayerControlsUiState()
) {
    private val mutableState = MutableStateFlow(initialState)

    val state: StateFlow<PlayerControlsUiState> = mutableState.asStateFlow()

    fun onSurfaceTap() {
        mutableState.update { currentState ->
            currentState.copy(
                isVisible = !currentState.isVisible,
                interactionVersion = currentState.interactionVersion + 1
            )
        }
    }

    fun markInteraction(keepVisible: Boolean = true) {
        mutableState.update { currentState ->
            currentState.copy(
                isVisible = if (keepVisible) true else currentState.isVisible,
                interactionVersion = currentState.interactionVersion + 1
            )
        }
    }

    fun onPlaybackChanged(isPlaying: Boolean) {
        if (!isPlaying) {
            mutableState.update { currentState ->
                if (currentState.isVisible) {
                    currentState
                } else {
                    currentState.copy(isVisible = true)
                }
            }
        }
    }

    fun hideControls() {
        mutableState.update { currentState ->
            if (currentState.isVisible) {
                currentState.copy(isVisible = false)
            } else {
                currentState
            }
        }
    }

    companion object {
        fun shouldAutoHide(
            isPlaying: Boolean,
            config: PlayerControlsConfig,
            state: PlayerControlsUiState
        ): Boolean {
            return config.enableAutoHide && isPlaying && state.isVisible
        }
    }
}
