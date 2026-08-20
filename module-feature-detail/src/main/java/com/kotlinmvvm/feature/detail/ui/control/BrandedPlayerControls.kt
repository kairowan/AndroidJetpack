package com.kotlinmvvm.feature.detail.ui.control

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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.api.PlayState
import com.kotlinmvvm.core.player.api.BufferingPlayState
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.feature.detail.ui.model.BrandedPlayerControlsConfig
import kotlinx.coroutines.delay

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 品牌播放器控制层，协调显隐时机并组合顶部、中心和底部控制区域
 */
@Composable
fun BrandedPlayerControls(
    player: VideoPlayerController,
    state: PlayerState,
    title: String,
    onBack: (() -> Unit)?,
    windowMode: VideoWindowMode,
    onWindowModeChanged: (VideoWindowMode) -> Unit,
    modifier: Modifier = Modifier,
    config: BrandedPlayerControlsConfig = BrandedPlayerControlsConfig(),
    endActions: @Composable RowScope.() -> Unit = {}
) {
    var visible by remember { mutableStateOf(true) }
    var interactionTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(interactionTick, state.isPlaying, config.autoHideMs) {
        if (state.isPlaying && visible) {
            delay(config.autoHideMs)
            visible = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            visible = !visible
            interactionTick += 1L
        }
    ) {
        if (state.playState == BufferingPlayState) {
            CircularProgressIndicator(
                color = config.accentColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize()) {
                BrandedPlayerTopBar(
                    title = title,
                    onBack = onBack,
                    windowMode = windowMode,
                    onWindowModeChanged = onWindowModeChanged,
                    endActions = endActions,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                BrandedPlayerCenterControls(
                    state = state,
                    accentColor = config.accentColor,
                    onReplay = { player.rewind(config.replayMs); interactionTick += 1L },
                    onToggle = { player.toggle(); interactionTick += 1L },
                    onForward = { player.forward(config.forwardMs); interactionTick += 1L },
                    modifier = Modifier.align(Alignment.Center)
                )
                BrandedPlayerBottomControls(
                    state = state,
                    config = config,
                    onSeek = { player.seekTo(it); interactionTick += 1L },
                    onSpeedSelected = { player.setSpeed(it); interactionTick += 1L },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
