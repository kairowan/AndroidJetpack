package com.kotlinmvvm.core.player

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class PlayerControlsConfig(
    val autoHideMs: Long = 3000L,
    val enableAutoHide: Boolean = true,
    val rewindMs: Long = 10_000L,
    val forwardMs: Long = 10_000L,
    val showTopBar: Boolean = true,
    val showCenterControls: Boolean = true,
    val showBottomBar: Boolean = true
)

data class PlayerControlsStyle(
    val topBarStartColor: Color = Color.Black.copy(alpha = 0.7f),
    val topBarEndColor: Color = Color.Transparent,
    val bottomBarStartColor: Color = Color.Transparent,
    val bottomBarEndColor: Color = Color.Black.copy(alpha = 0.72f),
    val iconColor: Color = Color.White,
    val timeTextColor: Color = Color.White,
    val progressPlayedColor: Color = Color.White,
    val progressBufferedColor: Color = Color.White.copy(alpha = 0.45f),
    val progressUnplayedColor: Color = Color.White.copy(alpha = 0.2f),
    val centerPlayButtonColor: Color = Color.Black.copy(alpha = 0.5f),
    val progressTrackHeight: Float = 4f
)

data class PlayerControlsIcons(
    val back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    val rewind: ImageVector = Icons.Default.Replay10,
    val play: ImageVector = Icons.Default.PlayArrow,
    val pause: ImageVector = Icons.Default.Pause,
    val forward: ImageVector = Icons.Default.Forward10
)

data class PlayerControlActions(
    val onRewind: (IPlayer, Long) -> Unit = { player, ms -> player.rewind(ms) },
    val onToggle: (IPlayer) -> Unit = { player -> player.toggle() },
    val onForward: (IPlayer, Long) -> Unit = { player, ms -> player.forward(ms) },
    val onSeekProgress: (IPlayer, Float) -> Unit = { player, progress -> player.seekTo(progress) }
)

/**
 * 播放器控制层
 */
@Composable
fun PlayerControls(
    player: IPlayer,
    modifier: Modifier = Modifier,
    title: String = "",
    onBack: (() -> Unit)? = null,
    config: PlayerControlsConfig = PlayerControlsConfig(),
    style: PlayerControlsStyle = PlayerControlsStyle(),
    icons: PlayerControlsIcons = PlayerControlsIcons(),
    actions: PlayerControlActions = PlayerControlActions(),
    extraControls: @Composable RowScope.() -> Unit = {}
) {
    val state = player.collectState()
    var visible by remember { mutableStateOf(true) }
    var lastTouch by remember { mutableLongStateOf(0L) }
    
    // 自动隐藏
    LaunchedEffect(lastTouch, state.isPlaying) {
        if (config.enableAutoHide && state.isPlaying && visible) {
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
                lastTouch = System.currentTimeMillis()
            }
    ) {
        // 缓冲指示器
        if (state.playState == PlayState.Buffering) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(Modifier.fillMaxSize()) {
                // 顶部
                if (config.showTopBar) {
                    TopBar(
                        title = title,
                        onBack = onBack,
                        icon = icons.back,
                        style = style,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
                
                // 中间
                if (config.showCenterControls) {
                    CenterControls(
                        state = state,
                        icons = icons,
                        style = style,
                        onRewind = {
                            actions.onRewind(player, config.rewindMs)
                            lastTouch = System.currentTimeMillis()
                        },
                        onToggle = {
                            actions.onToggle(player)
                            lastTouch = System.currentTimeMillis()
                        },
                        onForward = {
                            actions.onForward(player, config.forwardMs)
                            lastTouch = System.currentTimeMillis()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // 底部
                if (config.showBottomBar) {
                    BottomBar(
                        state = state,
                        style = style,
                        onSeek = {
                            actions.onSeekProgress(player, it)
                            lastTouch = System.currentTimeMillis()
                        },
                        extraControls = extraControls,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    onBack: (() -> Unit)?,
    icon: ImageVector,
    style: PlayerControlsStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(style.topBarStartColor, style.topBarEndColor)))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        onBack?.let {
            IconButton(onClick = it) {
                Icon(icon, null, tint = style.iconColor)
            }
        }
        Text(title, color = style.timeTextColor, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CenterControls(
    state: PlayerState,
    icons: PlayerControlsIcons,
    style: PlayerControlsStyle,
    onRewind: () -> Unit,
    onToggle: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onRewind, Modifier.size(48.dp)) {
            Icon(icons.rewind, null, tint = style.iconColor, modifier = Modifier.size(36.dp))
        }
        
        IconButton(
            onClick = onToggle,
            modifier = Modifier
                .size(64.dp)
                .background(style.centerPlayButtonColor, MaterialTheme.shapes.extraLarge)
        ) {
            Icon(
                if (state.isPlaying) icons.pause else icons.play,
                null,
                tint = style.iconColor,
                modifier = Modifier.size(40.dp)
            )
        }
        
        IconButton(onClick = onForward, Modifier.size(48.dp)) {
            Icon(icons.forward, null, tint = style.iconColor, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun BottomBar(
    state: PlayerState,
    style: PlayerControlsStyle,
    onSeek: (Float) -> Unit,
    extraControls: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(style.bottomBarStartColor, style.bottomBarEndColor)))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PlayerProgressBar(
            value = state.progress,
            bufferedValue = state.bufferedProgress,
            onValueChange = onSeek,
            style = style
        )
        
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${state.formatPosition()} / ${state.formatDuration()}",
                color = style.timeTextColor,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                extraControls()
            }
        }
    }
}

@Composable
private fun PlayerProgressBar(
    value: Float,
    bufferedValue: Float,
    onValueChange: (Float) -> Unit,
    style: PlayerControlsStyle
) {
    val played = value.coerceIn(0f, 1f)
    val buffered = bufferedValue.coerceIn(0f, 1f)
    val trackShape = RoundedCornerShape(percent = 50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(style.progressTrackHeight.dp)
                .background(style.progressUnplayedColor, trackShape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(buffered)
                .height(style.progressTrackHeight.dp)
                .background(style.progressBufferedColor, trackShape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(played)
                .height(style.progressTrackHeight.dp)
                .background(style.progressPlayedColor, trackShape)
        )

        Slider(
            value = played,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = style.progressPlayedColor,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}
