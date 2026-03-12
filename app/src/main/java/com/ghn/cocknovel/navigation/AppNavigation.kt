package com.ghn.cocknovel.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.navigation.AppNavigationState
import com.kotlinmvvm.core.navigation.AppRoute
import com.kotlinmvvm.core.navigation.AppShellState
import com.kotlinmvvm.feature.detail.VideoDetailRoute
import com.kotlinmvvm.feature.home.HomeRoute
import com.kotlinmvvm.feature.shorts.ShortsRoute

/**
 * @author 浩楠
 *
 * @date 2026-2-20
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: App 壳层导航，负责 Home/Shorts/Detail 的编排
 */

private sealed class BottomNavItem(
    val topLevelRoute: AppRoute.TopLevel,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(AppRoute.TopLevel.HOME, Icons.Default.Home, "首页")
    data object Shorts : BottomNavItem(AppRoute.TopLevel.SHORTS, Icons.Default.PlayArrow, "短视频")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    repository: EyepetizerRepository,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(BottomNavItem.Home, BottomNavItem.Shorts)
    val backStack = rememberNavBackStack(AppDestination.Home)
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
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute.topLevelRoute == item.topLevelRoute,
                            onClick = { navigateToTopLevel(item.topLevelRoute) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.Black,
                                unselectedIconColor = Color.Black,
                                unselectedTextColor = Color.Black,
                                indicatorColor = Color.Black
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            modifier = modifier
                .padding(paddingValues)
                .background(navContentBackground),
            onBack = { popDestination() },
            entryProvider = entryProvider(
                fallback = { key ->
                    NavEntry(key = key) {
                        Text("Unknown destination")
                    }
                }
            ) {
                entry<AppDestination.Home> {
                    HomeRoute(
                        repository = repository,
                        onVideoClick = { video ->
                            applyNavigationState(navigationState.openDetail(video))
                        }
                    )
                }
                entry<AppDestination.Shorts> {
                    ShortsRoute(
                        isActive = currentRoute is AppRoute.Shorts,
                        deactivateSignal = navigationState.shortsDeactivateSignal,
                        repository = repository,
                        onFullscreenChanged = { isFullscreen ->
                            shellState = navigationState.onShortsFullscreenChanged(isFullscreen).shellState
                        }
                    )
                }
                entry<AppDestination.Detail> { destination ->
                    val detailRoute = destination.toAppRoute() as? AppRoute.Detail
                    if (detailRoute == null) {
                        Text("Invalid detail destination")
                    } else {
                        VideoDetailRoute(
                            video = detailRoute.video,
                            onBack = { popDestination() }
                        )
                    }
                }
            }
        )
    }
}
