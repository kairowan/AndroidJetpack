package com.kotlinmvvm.core.player.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.defaults.PlayerControlsDefaults
import com.kotlinmvvm.core.player.defaults.PlayerDefaults

/**
 * 控制层行为配置。
 */
data class PlayerControlsConfig(
    val autoHideMs: Long = PlayerControlsDefaults.AUTO_HIDE_MS,
    val enableAutoHide: Boolean = true,
    val rewindMs: Long = PlayerDefaults.SEEK_INTERVAL_MS,
    val forwardMs: Long = PlayerDefaults.SEEK_INTERVAL_MS,
    val showTopBar: Boolean = true,
    val showCenterControls: Boolean = true,
    val showBottomBar: Boolean = true
)

/**
 * 控制层视觉样式。
 */
data class PlayerControlsStyle(
    val topBarStartColor: Color = Color.Black.copy(alpha = PlayerControlsDefaults.TOP_BAR_GRADIENT_START_ALPHA),
    val topBarEndColor: Color = Color.Transparent,
    val bottomBarStartColor: Color = Color.Transparent,
    val bottomBarEndColor: Color = Color.Black.copy(alpha = PlayerControlsDefaults.BOTTOM_BAR_GRADIENT_END_ALPHA),
    val iconColor: Color = Color.White,
    val timeTextColor: Color = Color.White,
    val progressPlayedColor: Color = Color.White,
    val progressBufferedColor: Color = Color.White.copy(alpha = PlayerControlsDefaults.PROGRESS_BUFFERED_ALPHA),
    val progressUnplayedColor: Color = Color.White.copy(alpha = PlayerControlsDefaults.PROGRESS_UNPLAYED_ALPHA),
    val centerPlayButtonColor: Color = Color.Black.copy(alpha = PlayerControlsDefaults.CENTER_PLAY_BUTTON_ALPHA),
    val progressTrackHeight: Dp = PlayerControlsDefaults.PROGRESS_TRACK_HEIGHT
)

/**
 * 控制层图标。
 */
data class PlayerControlsIcons(
    val back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    val rewind: ImageVector = Icons.Default.Replay10,
    val play: ImageVector = Icons.Default.PlayArrow,
    val pause: ImageVector = Icons.Default.Pause,
    val forward: ImageVector = Icons.Default.Forward10
)

/**
 * 控制层操作回调。
 */
data class PlayerControlActions(
    val onRewind: (IPlayer, Long) -> Unit = { player, ms -> player.rewind(ms) },
    val onToggle: (IPlayer) -> Unit = { player -> player.toggle() },
    val onForward: (IPlayer, Long) -> Unit = { player, ms -> player.forward(ms) },
    val onSeekProgress: (IPlayer, Float) -> Unit = { player, progress -> player.seekTo(progress) }
)
