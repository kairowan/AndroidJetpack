package com.ghn.cocknovel.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ghn.cocknovel.R
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.core.navigation.AppNavigationState
import com.kotlinmvvm.core.navigation.AppRoute
import com.kotlinmvvm.core.navigation.AppShellState
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.feature.detail.VideoDetailRoute
import com.kotlinmvvm.feature.home.HomeRoute
import com.kotlinmvvm.feature.shorts.ShortsRoute
import com.kotlinmvvm.core.ui.navigation.AppNavigationBar
import com.kotlinmvvm.core.ui.navigation.AppTopLevelDestination

/**
 * @author 浩楠
 *
 * @date 2026/7/24 12:05
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: App 壳层导航，负责 Home/Shorts/Detail 的编排
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    repository: FeedPageRepository,
    videoPlayerFactory: VideoPlayerFactory,
    onRootBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(HomeDestination)
    var shellState by rememberSaveable(stateSaver = AppShellStateSaver.Saver) {
        mutableStateOf(AppShellState())
    }
    val navigationState = AppNavigationState.fromBackStack(
        backStack = backStack.mapNotNull { (it as? AppDestination)?.toAppRoute() },
        shellState = shellState
    )
    val currentRoute = navigationState.currentRoute

    LaunchedEffect(currentRoute) {
        shellState = shellState.onDestinationChanged(currentRoute)
    }

    val navContentBackground = if (navigationState.usesDarkNavigationChrome()) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }
    val showBottomBar = navigationState.shouldShowBottomBar()

    fun syncBackStack(routes: List<AppRoute>) {
        while (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
        }
        routes.forEach { route ->
            backStack.add(route.toDestination())
        }
    }

    fun applyNavigationState(updatedState: AppNavigationState) {
        shellState = updatedState.shellState
        syncBackStack(updatedState.backStack)
    }

    fun navigateToTopLevel(route: AppRoute.TopLevel) {
        applyNavigationState(navigationState.navigateToTopLevel(route))
    }

    fun popDestination() {
        applyNavigationState(navigationState.pop())
    }

    Scaffold(
        containerColor = navContentBackground,
        bottomBar = {
            if (showBottomBar) {
                AppNavigationBar(
                    selected = AppTopLevelDestination.from(currentRoute.topLevelRoute),
                    onSelected = { destination -> navigateToTopLevel(destination.route) }
                )
            }
        }
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            modifier = modifier
                .padding(paddingValues)
                .background(navContentBackground),
            onBack = {
                if (backStack.size > 1) popDestination() else onRootBack()
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider(
                fallback = { key ->
                    NavEntry(key = key) {
                        Text(stringResource(R.string.unknown_destination))
                    }
                }
            ) {
                entry<HomeDestination> {
                    HomeRoute(
                        repository = repository,
                        onVideoClick = { video ->
                            applyNavigationState(navigationState.openDetail(video))
                        }
                    )
                }
                entry<ShortsDestination> {
                    ShortsRoute(
                        isActive = currentRoute is AppRoute.Shorts,
                        deactivateSignal = navigationState.shortsDeactivateSignal,
                        repository = repository,
                        videoPlayerFactory = videoPlayerFactory,
                        onFullscreenChanged = { isFullscreen ->
                            shellState = navigationState.onShortsFullscreenChanged(isFullscreen).shellState
                        }
                    )
                }
                entry<DetailDestination> { destination ->
                    val detailRoute = destination.toAppRoute() as? AppRoute.Detail
                    if (detailRoute == null) {
                        Text(stringResource(R.string.invalid_detail_destination))
                    } else {
                        VideoDetailRoute(
                            video = detailRoute.video,
                            videoPlayerFactory = videoPlayerFactory,
                            onBack = { popDestination() }
                        )
                    }
                }
            }
        )
    }
}
