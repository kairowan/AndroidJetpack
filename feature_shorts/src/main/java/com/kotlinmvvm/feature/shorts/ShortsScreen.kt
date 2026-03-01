package com.kotlinmvvm.feature.shorts

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.player.ShortsPager
import com.kotlinmvvm.core.player.ShortsItem
import com.kotlinmvvm.core.player.ShortsOverlay
import com.kotlinmvvm.core.player.rememberPlayer
import com.kotlinmvvm.core.ui.component.LoadingContent
import com.kotlinmvvm.core.ui.component.ErrorContent
import com.kotlinmvvm.core.ui.state.UiState

private data class VideoItem(
    val video: EyepetizerFeedItem.Video
) : ShortsItem {
    override val id: Any get() = video.id
    override val videoUrl: String get() = video.playUrl
}

private enum class ShortsFullscreenMode {
    NONE,
    PORTRAIT,
    LANDSCAPE
}

@Composable
fun ShortsRoute(
    modifier: Modifier = Modifier,
    repository: EyepetizerRepository = remember { EyepetizerRepository() },
    viewModel: ShortsViewModel = viewModel(factory = ShortsViewModel.factory(repository)),
    onFullscreenChanged: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val player = rememberPlayer()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var fullscreenModeName by rememberSaveable { mutableStateOf(ShortsFullscreenMode.NONE.name) }
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

    DisposableEffect(activity) {
        onDispose {
            onFullscreenChangedState(false)
            activity?.applyVideoWindowMode(ShortsFullscreenMode.NONE)
        }
    }

    BackHandler(enabled = isFullscreen) {
        updateFullscreenMode(ShortsFullscreenMode.NONE)
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when (val state = uiState) {
            is UiState.Loading -> LoadingContent()
            is UiState.Error -> ErrorContent(
                message = state.message,
                onRetry = { viewModel.loadShorts() }
            )
            is UiState.Success -> {
                val videos = state.data.items
                if (videos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无视频", color = Color.White)
                    }
                } else {
                    ShortsPager(
                        items = videos.map { VideoItem(it) },
                        player = player,
                        onPageChanged = { page ->
                            if (page >= videos.size - 3 && state.data.canLoadMore) {
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
