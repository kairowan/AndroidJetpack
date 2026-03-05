package com.ghn.cocknovel.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.feature.detail.VideoDetailScreen
import com.kotlinmvvm.feature.home.HomeRoute
import com.kotlinmvvm.feature.shorts.ShortsRoute
import kotlinx.serialization.Serializable

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
 * @Description: TODO
 */

@Serializable
private sealed interface AppDestination : NavKey {
    @Serializable
    data object Home : AppDestination
    @Serializable
    data object Shorts : AppDestination
    @Serializable
    data class Detail(
        val videoId: Int,
        val title: String,
        val description: String,
        val coverUrl: String,
        val playUrl: String,
        val category: String,
        val authorName: String,
        val authorIcon: String,
        val duration: Int
    ) : AppDestination
}

private sealed class BottomNavItem(
    val destination: AppDestination,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(AppDestination.Home, Icons.Default.Home, "首页")
    data object Shorts : BottomNavItem(AppDestination.Shorts, Icons.Default.PlayArrow, "短视频")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val navItems = listOf(BottomNavItem.Home, BottomNavItem.Shorts)
    val backStack = rememberNavBackStack(AppDestination.Home)
    val currentDestination = backStack.lastOrNull()
    var shortsFullscreen by remember { mutableStateOf(false) }

    val showBottomBar = currentDestination is AppDestination.Home ||
            (currentDestination is AppDestination.Shorts && !shortsFullscreen)

    fun navigateToTopLevel(destination: AppDestination) {
        if (currentDestination == destination) return
        while (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    fun popDestination() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination == item.destination,
                            onClick = { navigateToTopLevel(item.destination) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            modifier = modifier.padding(paddingValues),
            onBack = { popDestination() },
            entryProvider = entryProvider(
                fallback = { key ->
                    NavEntry(key = key) {
                        Text("Unknown destination")
                    }
                }
            ) {
                entry<AppDestination.Home> {
                    shortsFullscreen = false
                    HomeRoute(
                        onVideoClick = { video ->
                            backStack.add(video.toDetailDestination())
                        }
                    )
                }
                entry<AppDestination.Shorts> {
                    ShortsRoute(
                        onFullscreenChanged = { isFullscreen ->
                            shortsFullscreen = isFullscreen
                        }
                    )
                }
                entry<AppDestination.Detail> { destination ->
                    shortsFullscreen = false
                    VideoDetailScreen(
                        video = destination.toVideo(),
                        onBack = { popDestination() }
                    )
                }
            }
        )
    }
}

private fun EyepetizerFeedItem.Video.toDetailDestination(): AppDestination.Detail {
    return AppDestination.Detail(
        videoId = id,
        title = title,
        description = description,
        coverUrl = coverUrl,
        playUrl = playUrl,
        category = category,
        authorName = authorName,
        authorIcon = authorIcon,
        duration = duration
    )
}

private fun AppDestination.Detail.toVideo(): EyepetizerFeedItem.Video {
    return EyepetizerFeedItem.Video(
        id = videoId,
        title = title,
        description = description,
        coverUrl = coverUrl,
        playUrl = playUrl,
        category = category,
        authorName = authorName,
        authorIcon = authorIcon,
        duration = duration
    )
}
