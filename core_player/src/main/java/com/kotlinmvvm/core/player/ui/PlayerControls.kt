package com.kotlinmvvm.core.player.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.api.PlayState
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.defaults.PlayerControlsDefaults
import com.kotlinmvvm.core.player.ext.collectState
import com.kotlinmvvm.core.player.model.PlayerControlActions
import com.kotlinmvvm.core.player.model.PlayerControlsConfig
import com.kotlinmvvm.core.player.model.PlayerControlsIcons
import com.kotlinmvvm.core.player.model.PlayerControlsStyle
import kotlinx.coroutines.delay

/**
 * 播放器控制层。
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
        if (state.playState == PlayState.Buffering) {
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
                    TopBar(
                        title = title,
                        onBack = onBack,
                        icon = icons.back,
                        style = style,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                if (config.showCenterControls) {
                    CenterControls(
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
                    BottomBar(
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
            .padding(PlayerControlsDefaults.TOP_BAR_PADDING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        onBack?.let { handleBack ->
            IconButton(onClick = handleBack) {
                Icon(icon, contentDescription = null, tint = style.iconColor)
            }
        }
        Text(
            text = title,
            color = style.timeTextColor,
            style = MaterialTheme.typography.titleMedium
        )
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
        horizontalArrangement = Arrangement.spacedBy(PlayerControlsDefaults.CENTER_BUTTON_SPACING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onRewind, modifier = Modifier.size(PlayerControlsDefaults.SIDE_BUTTON_SIZE)) {
            Icon(
                imageVector = icons.rewind,
                contentDescription = null,
                tint = style.iconColor,
                modifier = Modifier.size(PlayerControlsDefaults.SIDE_ICON_SIZE)
            )
        }

        IconButton(
            onClick = onToggle,
            modifier = Modifier
                .size(PlayerControlsDefaults.PLAY_BUTTON_SIZE)
                .background(style.centerPlayButtonColor, MaterialTheme.shapes.extraLarge)
        ) {
            Icon(
                imageVector = if (state.isPlaying) icons.pause else icons.play,
                contentDescription = null,
                tint = style.iconColor,
                modifier = Modifier.size(PlayerControlsDefaults.PLAY_ICON_SIZE)
            )
        }

        IconButton(onClick = onForward, modifier = Modifier.size(PlayerControlsDefaults.SIDE_BUTTON_SIZE)) {
            Icon(
                imageVector = icons.forward,
                contentDescription = null,
                tint = style.iconColor,
                modifier = Modifier.size(PlayerControlsDefaults.SIDE_ICON_SIZE)
            )
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
            .padding(
                horizontal = PlayerControlsDefaults.BOTTOM_BAR_HORIZONTAL_PADDING,
                vertical = PlayerControlsDefaults.BOTTOM_BAR_VERTICAL_PADDING
            )
    ) {
        PlayerProgressBar(
            value = state.progress,
            bufferedValue = state.bufferedProgress,
            onValueChange = onSeek,
            style = style
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.formatPosition()} / ${state.formatDuration()}",
                color = style.timeTextColor,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(PlayerControlsDefaults.BOTTOM_BAR_VERTICAL_PADDING),
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
            .height(PlayerControlsDefaults.PROGRESS_SLIDER_HEIGHT),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(style.progressTrackHeight)
                .background(style.progressUnplayedColor, trackShape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(buffered)
                .height(style.progressTrackHeight)
                .background(style.progressBufferedColor, trackShape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(played)
                .height(style.progressTrackHeight)
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
