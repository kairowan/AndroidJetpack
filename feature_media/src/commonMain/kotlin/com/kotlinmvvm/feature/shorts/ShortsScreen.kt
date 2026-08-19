package com.kotlinmvvm.feature.shorts

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Shorts 四端共享的纯 CMP Screen，通过插槽接收平台图片和视频画面
 */
@Composable
fun ShortsScreen(
    pageModel: ShortsPageModel,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onCurrentPageChanged: (Int) -> Unit,
    onVideoDoubleTap: () -> Unit,
    onEnterPortraitFullscreen: () -> Unit,
    onEnterLandscapeFullscreen: () -> Unit,
    onToggleFullscreenOrientation: () -> Unit,
    onExitFullscreen: () -> Unit,
    authorImage: @Composable (ShortsVideoCardModel, Modifier) -> Unit,
    videoSurface: @Composable (ShortsVideoCardModel, Boolean, Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    val errorMessage = pageModel.errorMessage
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            pageModel.isLoading && pageModel.videos.isEmpty() -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage != null && pageModel.videos.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage, color = Color.White)
                    TextButton(onClick = onRetry) {
                        Text("重试")
                    }
                }
            }

            pageModel.videos.isEmpty() -> {
                Text(
                    text = pageModel.emptyMessage,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                val videos = pageModel.videos
                val pagerState = rememberPagerState(
                    initialPage = pageModel.currentPage.coerceIn(videos.indices),
                    pageCount = videos::size
                )

                LaunchedEffect(pageModel.currentPage, videos.size) {
                    val restoredPage = pageModel.currentPage.coerceIn(videos.indices)
                    if (pagerState.currentPage != restoredPage) {
                        pagerState.scrollToPage(restoredPage)
                    }
                }

                LaunchedEffect(
                    pagerState.currentPage,
                    videos.size,
                    pageModel.canLoadMore,
                    pageModel.isLoadingMore
                ) {
                    val currentPage = pagerState.currentPage
                    onCurrentPageChanged(currentPage)
                    if (
                        currentPage >= videos.lastIndex - LOAD_MORE_THRESHOLD &&
                        pageModel.canLoadMore &&
                        !pageModel.isLoadingMore
                    ) {
                        onLoadMore()
                    }
                }

                VerticalPager(
                    state = pagerState,
                    key = { index -> videos[index].id },
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val video = videos[page]
                    val isCurrent = page == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .pointerInput(video.id) {
                                detectTapGestures(onDoubleTap = { onVideoDoubleTap() })
                            }
                    ) {
                        videoSurface(video, isCurrent, Modifier.fillMaxSize())
                        ShortsOverlay(
                            video = video,
                            pageModel = pageModel,
                            isCurrent = isCurrent,
                            authorImage = authorImage,
                            onEnterPortraitFullscreen = onEnterPortraitFullscreen,
                            onEnterLandscapeFullscreen = onEnterLandscapeFullscreen,
                            onToggleFullscreenOrientation = onToggleFullscreenOrientation,
                            onExitFullscreen = onExitFullscreen,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }

                if (pageModel.isLoadingMore) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShortsOverlay(
    video: ShortsVideoCardModel,
    pageModel: ShortsPageModel,
    isCurrent: Boolean,
    authorImage: @Composable (ShortsVideoCardModel, Modifier) -> Unit,
    onEnterPortraitFullscreen: () -> Unit,
    onEnterLandscapeFullscreen: () -> Unit,
    onToggleFullscreenOrientation: () -> Unit,
    onExitFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                )
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            authorImage(
                video,
                Modifier
                    .size(40.dp)
                    .background(Color.Gray, MaterialTheme.shapes.extraLarge)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                video.authorHandle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            video.title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            maxLines = 2
        )
        Spacer(Modifier.height(8.dp))
        Text(
            video.categoryTag,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        if (isCurrent) {
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pageModel.isFullscreen) {
                    TextButton(onClick = onToggleFullscreenOrientation) {
                        Text(pageModel.controlsCopy.toggleOrientationLabel, color = Color.White)
                    }
                    TextButton(onClick = onExitFullscreen) {
                        Text(pageModel.controlsCopy.exitFullscreenLabel, color = Color.White)
                    }
                } else {
                    TextButton(onClick = onEnterPortraitFullscreen) {
                        Text(pageModel.controlsCopy.enterPortraitFullscreenLabel, color = Color.White)
                    }
                    TextButton(onClick = onEnterLandscapeFullscreen) {
                        Text(pageModel.controlsCopy.enterLandscapeFullscreenLabel, color = Color.White)
                    }
                }
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 2
