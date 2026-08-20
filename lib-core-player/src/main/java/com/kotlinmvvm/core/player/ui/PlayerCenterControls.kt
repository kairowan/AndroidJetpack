package com.kotlinmvvm.core.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kotlinmvvm.core.player.R
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.defaults.PlayerControlsDefaults
import com.kotlinmvvm.core.player.model.PlayerControlsIcons
import com.kotlinmvvm.core.player.model.PlayerControlsStyle

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 播放器中央控制区，提供后退、播放切换和快进操作
 */
@Composable
internal fun PlayerCenterControls(
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
                contentDescription = stringResource(R.string.core_player_rewind),
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
                imageVector = if (state.playbackRequested) icons.pause else icons.play,
                contentDescription = stringResource(
                    if (state.playbackRequested) R.string.core_player_pause
                    else R.string.core_player_play
                ),
                tint = style.iconColor,
                modifier = Modifier.size(PlayerControlsDefaults.PLAY_ICON_SIZE)
            )
        }

        IconButton(onClick = onForward, modifier = Modifier.size(PlayerControlsDefaults.SIDE_BUTTON_SIZE)) {
            Icon(
                imageVector = icons.forward,
                contentDescription = stringResource(R.string.core_player_forward),
                tint = style.iconColor,
                modifier = Modifier.size(PlayerControlsDefaults.SIDE_ICON_SIZE)
            )
        }
    }
}
