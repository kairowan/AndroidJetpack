package com.kotlinmvvm.feature.shorts

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlinmvvm.core.data.state.ShortsPlaybackState
import coil.compose.AsyncImage
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.data.repository.EyepetizerRepositoryFactory
import com.kotlinmvvm.core.player.provider.rememberPlayer
import com.kotlinmvvm.core.player.ui.ShortsOverlay
import com.kotlinmvvm.core.player.ui.ShortsPager
import com.kotlinmvvm.core.ui.component.LoadingContent
import com.kotlinmvvm.core.ui.component.ErrorContent
import com.kotlinmvvm.core.ui.base.viewModelFactory
import com.kotlinmvvm.feature.media.shared.ShortsPagePresenter

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
 * @Description: Shorts 短视频流页面
 */

@Composable
fun ShortsRoute(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    deactivateSignal: Int = 0,
    repository: EyepetizerRepository? = null,
    providedViewModel: ShortsViewModel? = null,
    onFullscreenChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val owner = remember(context) { context.findViewModelStoreOwner() }
    val routeRepository = repository ?: remember { EyepetizerRepositoryFactory.create() }
    val viewModel = providedViewModel ?: viewModel(
        viewModelStoreOwner = owner ?: checkNotNull(LocalViewModelStoreOwner.current),
        key = "shorts_root_view_model",
        factory = viewModelFactory {
            ShortsViewModel(routeRepository)
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val pageModel = remember(state, playbackState) {
        ShortsPagePresenter.present(
            state = state,
            playbackState = playbackState
        )
    }
    val player = rememberPlayer()
    val fullscreenMode = playbackState.fullscreenMode
    val isFullscreen = playbackState.isFullscreen
    val onFullscreenChangedState by rememberUpdatedState(onFullscreenChanged)

    LaunchedEffect(activity, fullscreenMode) {
        activity?.applyVideoWindowMode(fullscreenMode)
    }

    LaunchedEffect(isFullscreen) {
        onFullscreenChangedState(isFullscreen)
    }

    LaunchedEffect(isActive) {
        if (!isActive) {
            viewModel.exitFullscreen()
            player.pause()
            player.stop()
            player.clearVideoOutput()
            onFullscreenChangedState(false)
            activity?.applyVideoWindowMode(ShortsPlaybackState.FullscreenMode.NONE)
        }
    }

    LaunchedEffect(deactivateSignal) {
        if (deactivateSignal > 0) {
            viewModel.exitFullscreen()
            player.pause()
            player.stop()
            player.clearVideoOutput()
            onFullscreenChangedState(false)
            activity?.applyVideoWindowMode(ShortsPlaybackState.FullscreenMode.NONE)
        }
    }

    DisposableEffect(activity) {
        onDispose {
            player.pause()
            player.stop()
            player.clearVideoOutput()
            onFullscreenChangedState(false)
            activity?.applyVideoWindowMode(ShortsPlaybackState.FullscreenMode.NONE)
        }
    }

    BackHandler(enabled = isFullscreen) {
        viewModel.exitFullscreen()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isActive) Color.Black else Color.Transparent)
    ) {
        if (!isActive) return@Box

        when {
            pageModel.isLoading && pageModel.videos.isEmpty() -> LoadingContent()

            pageModel.errorMessage != null && pageModel.videos.isEmpty() -> {
                ErrorContent(
                    message = pageModel.errorMessage ?: "Unknown error",
                    onRetry = { viewModel.retry() }
                )
            }

            else -> {
                val videos = pageModel.videos
                if (videos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(pageModel.emptyMessage, color = Color.White)
                    }
                } else {
                    val restoredPage = pageModel.currentPage

                    LaunchedEffect(restoredPage) {
                        if (restoredPage != playbackState.currentPage) {
                            viewModel.updateCurrentPage(restoredPage)
                        }
                    }

                    val pagerState = rememberPagerState(
                        initialPage = restoredPage,
                        pageCount = { videos.size }
                    )

                    LaunchedEffect(pagerState.currentPage) {
                        viewModel.updateCurrentPage(pagerState.currentPage)
                    }

                    ShortsPager(
                        items = videos,
                        pagerState = pagerState,
                        player = player,
                        itemKey = { it.id },
                        videoUrlOf = { it.playUrl },
                        onPageChanged = { page ->
                            if (page >= videos.size - 3 && pageModel.canLoadMore && !pageModel.isLoadingMore) {
                                viewModel.loadMore()
                            }
                        }
                    ) { video, isCurrent ->
                        ShortsOverlay(Modifier.align(Alignment.BottomCenter)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = video.authorIcon,
                                    contentDescription = null,
                                    modifier = Modifier
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
                                color = Color.White.copy(0.7f)
                            )

                            if (isCurrent) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isFullscreen) {
                                        ShortsControlIconButton(
                                            icon = Icons.Default.Fullscreen,
                                            contentDescription = pageModel.controlsCopy.enterPortraitFullscreenLabel,
                                            onClick = viewModel::enterPortraitFullscreen
                                        )
                                        ShortsControlIconButton(
                                            icon = Icons.Default.ScreenRotation,
                                            contentDescription = pageModel.controlsCopy.enterLandscapeFullscreenLabel,
                                            onClick = viewModel::enterLandscapeFullscreen
                                        )
                                    } else {
                                        ShortsControlIconButton(
                                            icon = Icons.Default.ScreenRotation,
                                            contentDescription = pageModel.controlsCopy.toggleOrientationLabel,
                                            onClick = viewModel::toggleFullscreenOrientation
                                        )
                                        ShortsControlIconButton(
                                            icon = Icons.Default.FullscreenExit,
                                            contentDescription = pageModel.controlsCopy.exitFullscreenLabel,
                                            onClick = viewModel::exitFullscreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortsControlIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(Color.Black.copy(alpha = 0.35f), shape = MaterialTheme.shapes.extraLarge)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

private fun Activity.applyVideoWindowMode(mode: ShortsPlaybackState.FullscreenMode) {
    val targetOrientation = when (mode) {
        ShortsPlaybackState.FullscreenMode.NONE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        ShortsPlaybackState.FullscreenMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ShortsPlaybackState.FullscreenMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
    if (requestedOrientation != targetOrientation) {
        requestedOrientation = targetOrientation
    }

    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    if (mode == ShortsPlaybackState.FullscreenMode.NONE) {
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    } else {
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private tailrec fun Context.findViewModelStoreOwner(): ViewModelStoreOwner? {
    return when (this) {
        is ViewModelStoreOwner -> this
        is ContextWrapper -> baseContext.findViewModelStoreOwner()
        else -> null
    }
}
