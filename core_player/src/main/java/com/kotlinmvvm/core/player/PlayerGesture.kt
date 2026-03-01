package com.kotlinmvvm.core.player

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged

/**
 * @author 浩楠
 *
 * @date 2026-2-26
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

/**
 * 手势配置
 */
data class GestureConfig(
    val doubleTap: Boolean = true,
    val horizontalSeek: Boolean = true,
    val verticalVolume: Boolean = true,
    val verticalBrightness: Boolean = true
)

/**
 * 手势回调
 */
interface GestureCallback {
    fun onDoubleTap() {}
    fun onSeekStart() {}
    fun onSeeking(delta: Long) {}
    fun onSeekEnd(position: Long) {}
    fun onVolumeChange(delta: Float) {}
    fun onBrightnessChange(delta: Float) {}
}

/**
 * 播放器手势层
 */
@Composable
fun PlayerGestureDetector(
    player: IPlayer,
    modifier: Modifier = Modifier,
    config: GestureConfig = GestureConfig(),
    onBrightnessChange: (Float) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val state = player.collectState()
    var width by remember { mutableIntStateOf(1) }
    var seekStart by remember { mutableLongStateOf(0L) }
    var seekDelta by remember { mutableLongStateOf(0L) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { width = it.width }
            .then(
                if (config.doubleTap) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { player.toggle() })
                    }
                } else Modifier
            )
            .then(
                if (config.horizontalSeek) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                seekStart = state.position
                                seekDelta = 0
                            },
                            onDragEnd = {
                                player.seekTo(seekStart + seekDelta)
                            },
                            onHorizontalDrag = { _, drag ->
                                seekDelta += (drag * 100).toLong()
                            }
                        )
                    }
                } else Modifier
            )
            .then(
                if (config.verticalVolume || config.verticalBrightness) {
                    Modifier.pointerInput(Unit) {
                        detectVerticalDragGestures { change, drag ->
                            val isLeft = change.position.x < width / 2
                            val delta = -drag / 500f
                            
                            if (isLeft && config.verticalBrightness) {
                                onBrightnessChange(delta)
                            } else if (!isLeft && config.verticalVolume) {
                                val newVol = (player.state.value.volume + delta).coerceIn(0f, 1f)
                                player.setVolume(newVol)
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        content()
    }
}
