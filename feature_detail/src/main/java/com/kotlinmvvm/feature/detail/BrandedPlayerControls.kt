package com.kotlinmvvm.feature.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kotlinmvvm.core.player.IPlayer
import com.kotlinmvvm.core.player.PlayState
import com.kotlinmvvm.core.player.PlayerState
import kotlinx.coroutines.delay

/**
 * @author 浩楠
 *
 * @date 2026-2-28
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

data class BrandedPlayerControlsConfig(
    val autoHideMs: Long = 2800L,
    val replayMs: Long = 10_000L,
    val forwardMs: Long = 10_000L,
    val accentColor: Color = Color(0xFF2DE39B),
    val showSpeedControl: Boolean = true,
    val speedOptions: List<Float> = listOf(1f, 1.25f, 1.5f, 2f)
)

@Composable
fun BrandedPlayerControls(
    player: IPlayer,
    state: PlayerState,
    title: String,
    onBack: (() -> Unit)?,
    isFullscreen: Boolean,
    isLandscapeFullscreen: Boolean,
    onEnterPortraitFullscreen: () -> Unit,
    onEnterLandscapeFullscreen: () -> Unit,
    onExitFullscreen: () -> Unit,
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
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                visible = !visible
                interactionTick = System.currentTimeMillis()
            }
    ) {
        if (state.playState == PlayState.Buffering) {
            CircularProgressIndicator(
                color = config.accentColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(Modifier.fillMaxSize()) {
                TopBar(
                    title = title,
                    onBack = onBack,
                    isFullscreen = isFullscreen,
                    isLandscapeFullscreen = isLandscapeFullscreen,
                    onEnterPortraitFullscreen = onEnterPortraitFullscreen,
                    onEnterLandscapeFullscreen = onEnterLandscapeFullscreen,
                    onExitFullscreen = onExitFullscreen,
                    endActions = endActions,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                CenterBar(
                    state = state,
                    accentColor = config.accentColor,
                    onReplay = {
                        player.rewind(config.replayMs)
                        interactionTick = System.currentTimeMillis()
                    },
                    onToggle = {
                        player.toggle()
                        interactionTick = System.currentTimeMillis()
                    },
                    onForward = {
                        player.forward(config.forwardMs)
                        interactionTick = System.currentTimeMillis()
                    },
                    modifier = Modifier.align(Alignment.Center)
                )

                BottomBar(
                    state = state,
                    accentColor = config.accentColor,
                    onSeek = {
                        player.seekTo(it)
                        interactionTick = System.currentTimeMillis()
                    },
                    showSpeedControl = config.showSpeedControl,
                    speedOptions = config.speedOptions,
                    onNextSpeed = {
                        player.setSpeed(nextSpeed(state.speed, config.speedOptions))
                        interactionTick = System.currentTimeMillis()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    onBack: (() -> Unit)?,
    isFullscreen: Boolean,
    isLandscapeFullscreen: Boolean,
    onEnterPortraitFullscreen: () -> Unit,
    onEnterLandscapeFullscreen: () -> Unit,
    onExitFullscreen: () -> Unit,
    endActions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                )
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            }
            Text(
                text = title,
                color = Color.White,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!isFullscreen) {
                MiniIconButton(
                    icon = Icons.Default.Fullscreen,
                    contentDescription = "竖屏全屏",
                    onClick = onEnterPortraitFullscreen
                )
                MiniIconButton(
                    icon = Icons.Default.ScreenRotation,
                    contentDescription = "横屏全屏",
                    onClick = onEnterLandscapeFullscreen
                )
            } else {
                MiniIconButton(
                    icon = Icons.Default.ScreenRotation,
                    contentDescription = if (isLandscapeFullscreen) "切换竖屏" else "切换横屏",
                    onClick = {
                        if (isLandscapeFullscreen) {
                            onEnterPortraitFullscreen()
                        } else {
                            onEnterLandscapeFullscreen()
                        }
                    }
                )
                MiniIconButton(
                    icon = Icons.Default.FullscreenExit,
                    contentDescription = "退出全屏",
                    onClick = onExitFullscreen
                )
            }
            endActions()
        }
    }
}

@Composable
private fun CenterBar(
    state: PlayerState,
    accentColor: Color,
    onReplay: () -> Unit,
    onToggle: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        IconButton(onClick = onReplay, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "快退",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }

        IconButton(
            onClick = onToggle,
            modifier = Modifier
                .size(68.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(accentColor, accentColor.copy(alpha = 0.65f))
                    ),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (state.isPlaying) "暂停" else "播放",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }

        IconButton(onClick = onForward, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "快进",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun BottomBar(
    state: PlayerState,
    accentColor: Color,
    onSeek: (Float) -> Unit,
    showSpeedControl: Boolean,
    speedOptions: List<Float>,
    onNextSpeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                )
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        BrandedProgressBar(
            progress = state.progress,
            bufferedProgress = state.bufferedProgress,
            accentColor = accentColor,
            onSeek = onSeek
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${state.formatPosition()} / ${state.formatDuration()}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )

            if (showSpeedControl && speedOptions.isNotEmpty()) {
                AssistChip(
                    onClick = onNextSpeed,
                    label = {
                        Text(
                            text = "${state.speed}x",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = accentColor.copy(alpha = 0.28f),
                        labelColor = Color.White,
                        leadingIconContentColor = Color.White,
                        trailingIconContentColor = Color.White
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
private fun BrandedProgressBar(
    progress: Float,
    bufferedProgress: Float,
    accentColor: Color,
    onSeek: (Float) -> Unit
) {
    val played = progress.coerceIn(0f, 1f)
    val buffered = bufferedProgress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(Color.White.copy(alpha = 0.2f), shape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(buffered)
                .height(5.dp)
                .background(Color.White.copy(alpha = 0.5f), shape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(played)
                .height(5.dp)
                .background(accentColor, shape)
        )

        Slider(
            value = played,
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun MiniIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(34.dp)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

private fun nextSpeed(current: Float, candidates: List<Float>): Float {
    if (candidates.isEmpty()) return current
    val index = candidates.indexOfFirst { value -> value >= current }.coerceAtLeast(0)
    return if (index == candidates.lastIndex) candidates.first() else candidates[index + 1]
}
