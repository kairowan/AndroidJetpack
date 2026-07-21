package com.kotlinmvvm.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kotlinmvvm.app.R
import com.kotlinmvvm.app.config.AppNavigationMode
import com.kotlinmvvm.app.navigation.destination.registerHomeDestination
import com.kotlinmvvm.app.navigation.destination.registerShortsDestination
import com.kotlinmvvm.app.navigation.destination.registerVideoDetailDestination
import com.kotlinmvvm.app.navigation.model.AppRoute
import com.kotlinmvvm.app.navigation.model.HomeDestination
import com.kotlinmvvm.app.navigation.model.ShortsDestination
import com.kotlinmvvm.app.navigation.model.VideoDetailDestination
import com.kotlinmvvm.app.navigation.model.VideoRouteArguments
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.domain.feed.repository.FeedVideoRepository
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用多返回栈导航宿主，协调根返回退出、Activity 模式和自适应顶层导航布局
 */
@Composable
fun AppNavHost(
    pageRepository: FeedPageRepository,
    videoRepository: FeedVideoRepository,
    videoPlayerFactory: VideoPlayerFactory,
    taskObserver: ViewModelTaskObserver,
    navigationMode: AppNavigationMode,
    onOpenVideoActivity: (VideoRouteArguments) -> Unit,
    onWindowModeChanged: (VideoWindowMode) -> Unit,
    onRootBack: () -> Unit,
    modifier: Modifier = Modifier,
    navigationState: AppNavigationState = rememberAppNavigationState()
) {
    val currentRoute = navigationState.currentRoute
    var shortsFullscreen by rememberSaveable { mutableStateOf(false) }
    val contentBackground = when (currentRoute) {
        ShortsDestination, is VideoDetailDestination -> Color.Black
        else -> MaterialTheme.colorScheme.background
    }
    val showBottomBar = currentRoute == HomeDestination ||
        (currentRoute == ShortsDestination && !shortsFullscreen)

    fun openVideo(videoId: Int, source: FeedSource) {
        when (navigationMode) {
            AppNavigationMode.SINGLE_ACTIVITY ->
                navigationState.navigate(VideoDetailDestination(videoId, source))

            AppNavigationMode.MULTI_ACTIVITY ->
                onOpenVideoActivity(VideoRouteArguments(videoId, source))
        }
    }

    val provider = entryProvider<NavKey> {
        registerHomeDestination(pageRepository, taskObserver, ::openVideo)
        registerShortsDestination(
            pageRepository,
            videoPlayerFactory,
            taskObserver,
            { shortsFullscreen = it },
            onWindowModeChanged
        )
        registerVideoDetailDestination(
            videoRepository,
            videoPlayerFactory,
            taskObserver,
            { navigationState.pop() },
            onWindowModeChanged
        )
    }
    val homeEntries = rememberDecoratedNavEntries(
        backStack = navigationState.backStacks.getValue(HomeDestination),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = provider
    )
    val shortsEntries = rememberDecoratedNavEntries(
        backStack = navigationState.backStacks.getValue(ShortsDestination),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = provider
    )
    val visibleEntries = if (navigationState.topLevelRoute == HomeDestination) {
        homeEntries
    } else {
        homeEntries + shortsEntries
    }

    val navContent: @Composable (Modifier) -> Unit = { contentModifier ->
        NavDisplay(
            entries = visibleEntries,
            modifier = contentModifier.background(contentBackground),
            onBack = { if (!navigationState.pop()) onRootBack() }
        )
    }

    BoxWithConstraints(modifier = modifier) {
        val useNavigationRail = maxWidth >= EXPANDED_NAVIGATION_WIDTH && showBottomBar
        if (useNavigationRail) {
            Row(Modifier.fillMaxSize().background(contentBackground)) {
                AppNavigationRail(
                    selectedRoute = navigationState.topLevelRoute,
                    onNavigate = navigationState::navigateTopLevel
                )
                navContent(Modifier.weight(1f))
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = contentBackground,
                bottomBar = {
                    if (showBottomBar) {
                        AppBottomNavigationBar(
                            selectedRoute = navigationState.topLevelRoute,
                            onNavigate = navigationState::navigateTopLevel
                        )
                    }
                }
            ) { paddingValues ->
                navContent(Modifier.padding(paddingValues).consumeWindowInsets(paddingValues))
            }
        }
    }
}

/** 应用紧凑宽度顶层导航栏，切换时保留每个顶层页面自己的返回栈。 */
@Composable
private fun AppBottomNavigationBar(
    selectedRoute: AppRoute,
    onNavigate: (AppRoute) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = stringResource(R.string.navigation_home)
                )
            },
            label = { Text(stringResource(R.string.navigation_home)) },
            selected = selectedRoute == HomeDestination,
            onClick = { onNavigate(HomeDestination) }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.navigation_shorts)
                )
            },
            label = { Text(stringResource(R.string.navigation_shorts)) },
            selected = selectedRoute == ShortsDestination,
            onClick = { onNavigate(ShortsDestination) }
        )
    }
}

/** 应用中等及以上宽度顶层导航栏，为内容区域保留更多垂直空间。 */
@Composable
private fun AppNavigationRail(
    selectedRoute: AppRoute,
    onNavigate: (AppRoute) -> Unit
) {
    NavigationRail {
        NavigationRailItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = stringResource(R.string.navigation_home)
                )
            },
            label = { Text(stringResource(R.string.navigation_home)) },
            selected = selectedRoute == HomeDestination,
            onClick = { onNavigate(HomeDestination) }
        )
        NavigationRailItem(
            icon = {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.navigation_shorts)
                )
            },
            label = { Text(stringResource(R.string.navigation_shorts)) },
            selected = selectedRoute == ShortsDestination,
            onClick = { onNavigate(ShortsDestination) }
        )
    }
}

private val EXPANDED_NAVIGATION_WIDTH = 600.dp
