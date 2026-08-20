package com.kotlinmvvm.core.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.kotlinmvvm.core.player.R
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.api.PlayState
import com.kotlinmvvm.core.player.api.BufferingPlayState
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.model.PlayerControlActions
import com.kotlinmvvm.core.player.model.PlayerControlsConfig
import com.kotlinmvvm.core.player.model.PlayerControlsIcons
import com.kotlinmvvm.core.player.model.PlayerControlsStyle
import kotlinx.coroutines.delay

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用播放器控制层，根据外部状态渲染并把所有播放操作委托给控制器
 */
@Composable
fun PlayerControls(
    player: VideoPlayerController,
    state: PlayerState,
    modifier: Modifier = Modifier,
    title: String = "",
    onBack: (() -> Unit)? = null,
    config: PlayerControlsConfig = PlayerControlsConfig(),
    style: PlayerControlsStyle = PlayerControlsStyle(),
    icons: PlayerControlsIcons = PlayerControlsIcons(),
    actions: PlayerControlActions = PlayerControlActions(),
    extraControls: @Composable RowScope.() -> Unit = {}
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var interactionToken by remember { mutableIntStateOf(0) }

    fun markInteraction(keepVisible: Boolean = true) {
        if (keepVisible) controlsVisible = true
        interactionToken += 1
    }

    LaunchedEffect(state.isPlaying) {
        if (!state.isPlaying) {
            controlsVisible = true
        }
    }

    LaunchedEffect(
        config.enableAutoHide,
        config.autoHideMs,
        state.isPlaying,
        controlsVisible,
        interactionToken
    ) {
        if (!config.enableAutoHide || !state.isPlaying || !controlsVisible) return@LaunchedEffect
        delay(config.autoHideMs)
        controlsVisible = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                controlsVisible = !controlsVisible
                interactionToken += 1
            }
    ) {
        if (state.playState == BufferingPlayState) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(Modifier.fillMaxSize()) {
                if (config.showTopBar) {
                    PlayerTopBar(
                        title = title,
                        onBack = onBack,
                        icon = icons.back,
                        contentDescription = stringResource(R.string.core_player_back),
                        style = style,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                if (config.showCenterControls) {
                    PlayerCenterControls(
                        state = state,
                        icons = icons,
                        style = style,
                        onRewind = {
                            actions.onRewind(player, config.rewindMs)
                            markInteraction()
                        },
                        onToggle = {
                            actions.onToggle(player)
                            markInteraction()
                        },
                        onForward = {
                            actions.onForward(player, config.forwardMs)
                            markInteraction()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (config.showBottomBar) {
                    PlayerBottomControls(
                        state = state,
                        style = style,
                        onSeek = {
                            actions.onSeekProgress(player, it)
                            markInteraction()
                        },
                        extraControls = extraControls,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
