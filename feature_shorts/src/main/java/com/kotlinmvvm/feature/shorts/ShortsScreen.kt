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
import androidx.compose.runtime.saveable.rememberSaveable
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
import coil.compose.AsyncImage
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.player.provider.rememberPlayer
import com.kotlinmvvm.core.player.ui.ShortsOverlay
import com.kotlinmvvm.core.player.ui.ShortsPager
import com.kotlinmvvm.core.ui.component.LoadingContent
import com.kotlinmvvm.core.ui.component.ErrorContent
import com.kotlinmvvm.core.ui.base.viewModelFactory
import com.kotlinmvvm.feature.shorts.model.ShortsFullscreenMode
import com.kotlinmvvm.feature.shorts.model.VideoItem

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
 * @Description: TODO
 */

@Composable
fun ShortsRoute(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    deactivateSignal: Int = 0,
    providedViewModel: ShortsViewModel? = null,
    onFullscreenChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val owner = remember(context) { context.findViewModelStoreOwner() }
    val repository = remember { EyepetizerRepository() }
    val viewModel = providedViewModel ?: viewModel(
        viewModelStoreOwner = owner ?: checkNotNull(LocalViewModelStoreOwner.current),
        key = "shorts_root_view_model",
        factory = viewModelFactory {
            ShortsViewModel(repository)
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val player = rememberPlayer()
    var fullscreenModeName by rememberSaveable { mutableStateOf(ShortsFullscreenMode.NONE.name) }
    var savedPageIndex by rememberSaveable { mutableIntStateOf(0) }
    val fullscreenMode = remember(fullscreenModeName) { ShortsFullscreenMode.valueOf(fullscreenModeName) }
    val isFullscreen = fullscreenMode != ShortsFullscreenMode.NONE
    val onFullscreenChangedState by rememberUpdatedState(onFullscreenChanged)

    fun updateFullscreenMode(mode: ShortsFullscreenMode) {
        fullscreenModeName = mode.name
    }

    LaunchedEffect(activity, fullscreenMode) {
        activity?.applyVideoWindowMode(fullscreenMode)
    }

    LaunchedEffect(isFullscreen) {
        onFullscreenChangedState(isFullscreen)
    }

    LaunchedEffect(isActive) {
        if (!isActive) {
            updateFullscreenMode(ShortsFullscreenMode.NONE)
            player.pause()
            player.stop()
            player.clearVideoOutput()
            onFullscreenChangedState(false)
            activity?.applyVideoWindowMode(ShortsFullscreenMode.NONE)
        }
    }

    LaunchedEffect(deactivateSignal) {
        if (deactivateSignal > 0) {
            updateFullscreenMode(ShortsFullscreenMode.NONE)
            player.pause()
            player.stop()
            player.clearVideoOutput()
            onFullscreenChangedState(false)
            activity?.applyVideoWindowMode(ShortsFullscreenMode.NONE)
        }
    }

    DisposableEffect(activity) {
        onDispose {
            player.pause()
            player.stop()
            player.clearVideoOutput()
            onFullscreenChangedState(false)
            activity?.applyVideoWindowMode(ShortsFullscreenMode.NONE)
        }
    }

    BackHandler(enabled = isFullscreen) {
        updateFullscreenMode(ShortsFullscreenMode.NONE)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isActive) Color.Black else Color.Transparent)
    ) {
        if (!isActive) return@Box

        when {
            state.isLoading && state.items.isEmpty() -> LoadingContent()

            state.errorMessage != null && state.items.isEmpty() -> {
                ErrorContent(
                    message = state.errorMessage ?: "Unknown error",
                    onRetry = { viewModel.retry() }
                )
            }

            else -> {
                val videos = state.items
                if (videos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无视频", color = Color.White)
                    }
                } else {
                    val restoredPage = savedPageIndex.coerceIn(0, videos.lastIndex.coerceAtLeast(0))

                    LaunchedEffect(restoredPage) {
                        if (restoredPage != savedPageIndex) {
                            savedPageIndex = restoredPage
                        }
                    }

                    val pagerState = rememberPagerState(
                        initialPage = restoredPage,
                        pageCount = { videos.size }
                    )

                    LaunchedEffect(pagerState.currentPage) {
                        savedPageIndex = pagerState.currentPage
                    }

                    ShortsPager(
                        items = videos.map { VideoItem(it) },
                        pagerState = pagerState,
                        player = player,
                        onPageChanged = { page ->
                            if (page >= videos.size - 3 && state.canLoadMore && !state.isLoadingMore) {
                                viewModel.loadMore()
                            }
                        }
                    ) { item, isCurrent ->
                        ShortsOverlay(Modifier.align(Alignment.BottomCenter)) {
                            val video = item.video

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
                                    "@${video.authorName}",
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
                                "#${video.category}",
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
                                            contentDescription = "竖屏全屏",
                                            onClick = { updateFullscreenMode(ShortsFullscreenMode.PORTRAIT) }
                                        )
                                        ShortsControlIconButton(
                                            icon = Icons.Default.ScreenRotation,
                                            contentDescription = "横屏全屏",
                                            onClick = { updateFullscreenMode(ShortsFullscreenMode.LANDSCAPE) }
                                        )
                                    } else {
                                        ShortsControlIconButton(
                                            icon = Icons.Default.ScreenRotation,
                                            contentDescription = if (fullscreenMode == ShortsFullscreenMode.LANDSCAPE) "切换竖屏" else "切换横屏",
                                            onClick = {
                                                updateFullscreenMode(
                                                    if (fullscreenMode == ShortsFullscreenMode.LANDSCAPE) {
                                                        ShortsFullscreenMode.PORTRAIT
                                                    } else {
                                                        ShortsFullscreenMode.LANDSCAPE
                                                    }
                                                )
                                            }
                                        )
                                        ShortsControlIconButton(
                                            icon = Icons.Default.FullscreenExit,
                                            contentDescription = "退出全屏",
                                            onClick = { updateFullscreenMode(ShortsFullscreenMode.NONE) }
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

private fun Activity.applyVideoWindowMode(mode: ShortsFullscreenMode) {
    val targetOrientation = when (mode) {
        ShortsFullscreenMode.NONE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        ShortsFullscreenMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ShortsFullscreenMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
    if (requestedOrientation != targetOrientation) {
        requestedOrientation = targetOrientation
    }

    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    if (mode == ShortsFullscreenMode.NONE) {
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
