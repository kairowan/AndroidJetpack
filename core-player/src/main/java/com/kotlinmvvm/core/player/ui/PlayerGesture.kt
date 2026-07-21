package com.kotlinmvvm.core.player.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.ext.collectState
import com.kotlinmvvm.core.player.model.GestureConfig

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 播放器手势检测层，按配置组合双击、水平跳播、音量和亮度调节
 */
@Composable
fun PlayerGestureDetector(
    player: VideoPlayerController,
    modifier: Modifier = Modifier,
    config: GestureConfig = GestureConfig(),
    onBrightnessChange: (Float) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val state = player.collectState()
    val latestPosition by rememberUpdatedState(state.position)
    var widthPx by remember { mutableIntStateOf(1) }
    var gestureVolume by remember(player) { mutableFloatStateOf(state.volume) }

    LaunchedEffect(state.volume) {
        gestureVolume = state.volume
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { widthPx = it.width.coerceAtLeast(1) }
            .applyDoubleTapGesture(enabled = config.doubleTap) {
                player.toggle()
            }
            .applyHorizontalSeekGesture(
                enabled = config.horizontalSeek,
                seekMsPerPixel = config.seekMsPerPixel,
                readCurrentPosition = { latestPosition },
                onSeekTo = { player.seekTo(it) }
            )
            .applyVerticalAdjustGesture(
                enabled = config.verticalVolume || config.verticalBrightness,
                readViewWidth = { widthPx },
                verticalDragDivisor = config.verticalDragDivisor
            ) { isLeftArea, delta ->
                if (isLeftArea && config.verticalBrightness) {
                    onBrightnessChange(delta)
                } else if (!isLeftArea && config.verticalVolume) {
                    gestureVolume = (gestureVolume + delta).coerceIn(0f, 1f)
                    player.setVolume(gestureVolume)
                }
            }
    ) {
        content()
    }
}

private fun Modifier.applyDoubleTapGesture(
    enabled: Boolean,
    onDoubleTap: () -> Unit
): Modifier {
    if (!enabled) return this
    return pointerInput(enabled) {
        detectTapGestures(onDoubleTap = { onDoubleTap() })
    }
}

private fun Modifier.applyHorizontalSeekGesture(
    enabled: Boolean,
    seekMsPerPixel: Long,
    readCurrentPosition: () -> Long,
    onSeekTo: (Long) -> Unit
): Modifier {
    if (!enabled) return this
    return pointerInput(enabled, seekMsPerPixel) {
        var seekStart = 0L
        var seekDelta = 0L

        detectHorizontalDragGestures(
            onDragStart = {
                seekStart = readCurrentPosition()
                seekDelta = 0L
            },
            onHorizontalDrag = { _, dragAmount ->
                seekDelta += (dragAmount * seekMsPerPixel).toLong()
            },
            onDragEnd = {
                onSeekTo(seekStart + seekDelta)
            }
        )
    }
}

private fun Modifier.applyVerticalAdjustGesture(
    enabled: Boolean,
    readViewWidth: () -> Int,
    verticalDragDivisor: Float,
    onAdjust: (isLeftArea: Boolean, delta: Float) -> Unit
): Modifier {
    if (!enabled) return this
    return pointerInput(enabled, verticalDragDivisor) {
        detectVerticalDragGestures { change, dragAmount ->
            val isLeftArea = change.position.x < readViewWidth() / 2f
            val delta = -dragAmount / verticalDragDivisor
            onAdjust(isLeftArea, delta)
        }
    }
}
