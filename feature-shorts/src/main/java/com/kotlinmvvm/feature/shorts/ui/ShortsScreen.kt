package com.kotlinmvvm.feature.shorts.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.core.ui.feedback.ErrorContent
import com.kotlinmvvm.core.ui.feedback.LoadingContent
import com.kotlinmvvm.feature.shorts.R
import com.kotlinmvvm.feature.shorts.presentation.ShortsUiState
import com.kotlinmvvm.feature.shorts.ui.component.ShortsFeed

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 短视频无状态页面，根据加载状态选择反馈内容或可连续播放的视频流
 */
@Composable
fun ShortsScreen(
    uiState: ShortsUiState,
    player: VideoPlayerController,
    windowMode: VideoWindowMode,
    onWindowModeChanged: (VideoWindowMode) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = uiState.loadError?.let { stringResource(it.messageResource) }

    LaunchedEffect(errorMessage, uiState.videos.isNotEmpty()) {
        if (errorMessage != null && uiState.videos.isNotEmpty()) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Black,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(Color.Black)) {
            when {
                uiState.isInitialLoading -> LoadingContent()
                uiState.loadError != null && uiState.videos.isEmpty() -> ErrorContent(
                    message = errorMessage,
                    onRetry = onRetry
                )
                uiState.videos.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.shorts_empty), color = Color.White)
                }
                else -> ShortsFeed(
                    videos = uiState.videos,
                    canLoadMore = uiState.canLoadMore,
                    isLoadingMore = uiState.isLoadingMore,
                    player = player,
                    windowMode = windowMode,
                    onWindowModeChanged = onWindowModeChanged,
                    onLoadMore = onLoadMore
                )
            }
        }
    }
}

@get:StringRes
private val FeedLoadError.messageResource: Int
    get() = when (this) {
        FeedLoadError.NO_CONNECTION -> R.string.shorts_error_no_connection
        FeedLoadError.TIMEOUT -> R.string.shorts_error_timeout
        FeedLoadError.UNAUTHORIZED -> R.string.shorts_error_unauthorized
        FeedLoadError.SERVER -> R.string.shorts_error_server
        FeedLoadError.INVALID_RESPONSE -> R.string.shorts_error_invalid_response
        FeedLoadError.NOT_FOUND -> R.string.shorts_error_not_found
        FeedLoadError.UNKNOWN -> R.string.shorts_error_unknown
    }
