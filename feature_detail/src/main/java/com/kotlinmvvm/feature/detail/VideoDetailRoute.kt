package com.kotlinmvvm.feature.detail

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.ui.base.viewModelFactory

@Composable
fun VideoDetailRoute(
    video: EyepetizerFeedItem.Video,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    providedViewModel: VideoDetailViewModel? = null
) {
    val context = LocalContext.current
    val owner = remember(context) { context.findViewModelStoreOwner() }
    val viewModelKey = remember(video.id) { "video_detail_${video.id}" }
    val viewModel = providedViewModel ?: viewModel(
        viewModelStoreOwner = owner ?: checkNotNull(LocalViewModelStoreOwner.current),
        key = viewModelKey,
        factory = viewModelFactory { VideoDetailViewModel() }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    VideoDetailScreen(
        video = video,
        state = state,
        onBack = {
            if (viewModel.onBackPressed()) {
                onBack()
            }
        },
        onEnterPortraitFullscreen = viewModel::enterPortraitFullscreen,
        onEnterLandscapeFullscreen = viewModel::enterLandscapeFullscreen,
        onExitFullscreen = viewModel::exitFullscreen,
        onSavePlaybackSnapshot = viewModel::syncPlaybackSnapshot,
        modifier = modifier
    )
}

private tailrec fun Context.findViewModelStoreOwner(): ViewModelStoreOwner? {
    return when (this) {
        is ViewModelStoreOwner -> this
        is ContextWrapper -> baseContext.findViewModelStoreOwner()
        else -> null
    }
}
