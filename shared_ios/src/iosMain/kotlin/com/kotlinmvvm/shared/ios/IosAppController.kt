@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.kotlinmvvm.shared.ios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.kotlinmvvm.core.data.repository.EyepetizerRepositoryFactory
import com.kotlinmvvm.core.designsystem.theme.AppTheme
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.feature.detail.VideoDetailScreen
import com.kotlinmvvm.feature.home.HomeScreen
import com.kotlinmvvm.feature.home.shared.HomeFeedPagePresenter
import com.kotlinmvvm.feature.home.shared.HomeFeedStateHolder
import com.kotlinmvvm.feature.media.shared.ShortsFeedStateHolder
import com.kotlinmvvm.feature.media.shared.ShortsPagePresenter
import com.kotlinmvvm.feature.media.shared.ShortsPlaybackStateHolder
import com.kotlinmvvm.feature.media.shared.VideoDetailPagePresenter
import com.kotlinmvvm.feature.media.shared.VideoDetailState
import com.kotlinmvvm.feature.shorts.ShortsScreen
import com.kotlinmvvm.shared.ui.navigation.AppNavigationBar
import com.kotlinmvvm.shared.ui.navigation.AppTopLevelDestination
import platform.UIKit.UIViewController

/**
 * SwiftUI 只持有这个 Compose 控制器；页面、状态与导航均复用 CMP。
 */
class IosAppController {
    fun makeViewController(): UIViewController = ComposeUIViewController {
        AppTheme {
            IosApp()
        }
    }
}

@Composable
private fun IosApp() {
    val scope = rememberCoroutineScope()
    val repository = remember { EyepetizerRepositoryFactory.create() }
    val homeStateHolder = remember { HomeFeedStateHolder(scope, repository) }
    val shortsStateHolder = remember { ShortsFeedStateHolder(scope, repository) }
    val shortsPlaybackStateHolder = remember { ShortsPlaybackStateHolder() }
    val shortsPlayer = remember { IosVideoPlayer() }

    val homeState by homeStateHolder.state.collectAsState()
    val selectedSource by homeStateHolder.feedSource.collectAsState()
    val shortsState by shortsStateHolder.state.collectAsState()
    val shortsPlaybackState by shortsPlaybackStateHolder.state.collectAsState()
    var selectedDestination by remember { mutableStateOf(AppTopLevelDestination.HOME) }
    var detailVideo by remember { mutableStateOf<EyepetizerFeedItem.Video?>(null) }

    val homePageModel = remember(homeState, selectedSource) {
        HomeFeedPagePresenter.present(homeState, selectedSource)
    }
    val shortsPageModel = remember(shortsState, shortsPlaybackState) {
        ShortsPagePresenter.present(shortsState, shortsPlaybackState)
    }

    LaunchedEffect(Unit) {
        homeStateHolder.loadInitial()
        shortsStateHolder.loadInitial()
    }
    LaunchedEffect(
        selectedDestination,
        detailVideo,
        shortsPageModel.currentPage,
        shortsPageModel.videos
    ) {
        val activeVideo = shortsPageModel.videos.getOrNull(shortsPageModel.currentPage)
        if (
            selectedDestination == AppTopLevelDestination.SHORTS &&
            detailVideo == null &&
            activeVideo != null
        ) {
            shortsPlayer.play(activeVideo.playUrl)
        } else {
            shortsPlayer.pause()
        }
    }
    DisposableEffect(shortsPlayer) {
        onDispose(shortsPlayer::release)
    }

    val showBottomBar = detailVideo == null && !shortsPlaybackState.isFullscreen
    val contentColor = if (selectedDestination == AppTopLevelDestination.SHORTS) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }

    Scaffold(
        containerColor = contentColor,
        bottomBar = {
            if (showBottomBar) {
                AppNavigationBar(
                    selected = selectedDestination,
                    onSelected = { destination ->
                        detailVideo = null
                        shortsPlaybackStateHolder.exitFullscreen()
                        selectedDestination = destination
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val video = detailVideo
            when {
                video != null -> IosVideoDetail(
                    video = video,
                    onBack = { detailVideo = null },
                    modifier = Modifier.fillMaxSize()
                )

                selectedDestination == AppTopLevelDestination.HOME -> HomeScreen(
                    pageModel = homePageModel,
                    onSourceSelected = homeStateHolder::switchSource,
                    onRetry = homeStateHolder::retry,
                    onRefresh = homeStateHolder::refresh,
                    onLoadMore = homeStateHolder::loadMore,
                    onVideoClick = { selectedVideo ->
                        detailVideo = homeState.items
                            .filterIsInstance<EyepetizerFeedItem.Video>()
                            .firstOrNull { item -> item.id == selectedVideo.id }
                    },
                    image = { url, contentDescription, imageModifier ->
                        IosRemoteImage(
                            url = url,
                            contentDescription = contentDescription,
                            modifier = imageModifier
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )

                else -> ShortsScreen(
                    pageModel = shortsPageModel,
                    onRetry = shortsStateHolder::retry,
                    onLoadMore = shortsStateHolder::loadMore,
                    onCurrentPageChanged = shortsPlaybackStateHolder::updateCurrentPage,
                    onVideoDoubleTap = shortsPlayer::toggle,
                    onEnterPortraitFullscreen = shortsPlaybackStateHolder::enterPortraitFullscreen,
                    onEnterLandscapeFullscreen = shortsPlaybackStateHolder::enterLandscapeFullscreen,
                    onToggleFullscreenOrientation = shortsPlaybackStateHolder::toggleFullscreenOrientation,
                    onExitFullscreen = shortsPlaybackStateHolder::exitFullscreen,
                    authorImage = { shortsVideo, imageModifier ->
                        IosRemoteImage(
                            url = shortsVideo.authorIcon,
                            contentDescription = shortsVideo.authorName,
                            modifier = imageModifier
                        )
                    },
                    videoSurface = { _, isCurrent, surfaceModifier ->
                        if (isCurrent) {
                            IosVideoSurface(
                                player = shortsPlayer,
                                showPlaybackControls = false,
                                modifier = surfaceModifier
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun IosVideoDetail(
    video: EyepetizerFeedItem.Video,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = remember(video.id) { IosVideoPlayer() }
    val pageModel = remember(video) {
        VideoDetailPagePresenter.present(video, VideoDetailState())
    }

    LaunchedEffect(player, video.playUrl) {
        player.play(video.playUrl)
    }
    DisposableEffect(player) {
        onDispose(player::release)
    }

    Box(modifier) {
        VideoDetailScreen(
            pageModel = pageModel,
            player = { isFullscreen, playerModifier ->
                IosVideoSurface(
                    player = player,
                    showPlaybackControls = true,
                    modifier = if (isFullscreen) {
                        playerModifier.fillMaxSize()
                    } else {
                        playerModifier
                            .fillMaxWidth()
                            .aspectRatio(VIDEO_ASPECT_RATIO)
                    }
                )
            },
            authorImage = { url, contentDescription, imageModifier ->
                IosRemoteImage(
                    url = url,
                    contentDescription = contentDescription,
                    modifier = imageModifier
                )
            },
            modifier = Modifier.fillMaxSize()
        )
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text("返回", color = Color.White)
        }
    }
}

private const val VIDEO_ASPECT_RATIO = 16f / 9f
