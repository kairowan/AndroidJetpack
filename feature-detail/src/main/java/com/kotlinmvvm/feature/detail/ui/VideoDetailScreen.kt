package com.kotlinmvvm.feature.detail.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.api.PlayerState
import com.kotlinmvvm.core.player.ui.FullscreenVideoPlayer
import com.kotlinmvvm.core.player.ui.InlineVideoPlayer
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.feature.detail.ui.component.VideoMetadataContent
import com.kotlinmvvm.feature.detail.ui.control.BrandedPlayerControls
import com.kotlinmvvm.feature.detail.ui.model.BrandedPlayerControlsConfig

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情无状态页面，根据窗口模式组合播放器、品牌控制栏和视频元数据
 */
@Composable
fun VideoDetailScreen(
    video: FeedVideo,
    player: VideoPlayerController,
    windowMode: VideoWindowMode,
    controlsConfig: BrandedPlayerControlsConfig,
    onBack: () -> Unit,
    onWindowModeChanged: (VideoWindowMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFullscreen = windowMode != VideoWindowMode.INLINE
    val handleBack = {
        if (isFullscreen) onWindowModeChanged(VideoWindowMode.INLINE) else onBack()
    }
    val controls: @Composable BoxScope.(VideoPlayerController, PlayerState) -> Unit = { currentPlayer, state ->
        BrandedPlayerControls(
            player = currentPlayer,
            state = state,
            title = video.title,
            onBack = handleBack,
            windowMode = windowMode,
            onWindowModeChanged = onWindowModeChanged,
            config = controlsConfig
        )
    }

    if (isFullscreen) {
        FullscreenVideoPlayer(
            player = player,
            title = video.title,
            onBack = handleBack,
            controlsContent = controls,
            modifier = modifier
        )
    } else {
        BoxWithConstraints(
            modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            if (maxWidth >= EXPANDED_DETAIL_WIDTH) {
                Row(Modifier.fillMaxSize()) {
                    InlineVideoPlayer(
                        player = player,
                        title = video.title,
                        onBack = handleBack,
                        controlsContent = controls,
                        modifier = Modifier.weight(3f).align(Alignment.CenterVertically)
                    )
                    VideoMetadataContent(video, Modifier.weight(2f))
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    InlineVideoPlayer(
                        player = player,
                        title = video.title,
                        onBack = handleBack,
                        controlsContent = controls
                    )
                    VideoMetadataContent(video)
                }
            }
        }
    }
}

private val EXPANDED_DETAIL_WIDTH = 840.dp
