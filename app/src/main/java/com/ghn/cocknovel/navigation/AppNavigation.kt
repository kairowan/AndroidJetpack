package com.ghn.cocknovel.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.feature.detail.VideoDetailScreen
import com.kotlinmvvm.feature.home.HomeRoute
import com.kotlinmvvm.feature.shorts.ShortsRoute
import java.net.URLDecoder
import java.net.URLEncoder

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

object AppDestinations {
    const val HOME = "home"
    const val SHORTS = "shorts"
    const val DETAIL = "detail/{videoId}/{title}/{coverUrl}/{playUrl}/{category}/{authorName}/{authorIcon}/{duration}/{description}"
    
    fun detailRoute(video: EyepetizerFeedItem.Video): String {
        return "detail/${video.id}/${encode(video.title)}/${encode(video.coverUrl)}/${encode(video.playUrl)}/${encode(video.category)}/${encode(video.authorName)}/${encode(video.authorIcon)}/${video.duration}/${encode(video.description)}"
    }
    
    private fun encode(s: String) = URLEncoder.encode(s, "UTF-8")
}

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem(AppDestinations.HOME, Icons.Default.Home, "首页")
    object Shorts : BottomNavItem(AppDestinations.SHORTS, Icons.Default.PlayArrow, "短视频")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navItems = listOf(BottomNavItem.Home, BottomNavItem.Shorts)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var shortsFullscreen by remember { mutableStateOf(false) }
    
    // 判断是否显示底部导航
    val showBottomBar = currentRoute in listOf(AppDestinations.HOME, AppDestinations.SHORTS) && !shortsFullscreen

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(AppDestinations.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.HOME,
            modifier = modifier.padding(paddingValues)
        ) {
            composable(AppDestinations.HOME) {
                HomeRoute(
                    onVideoClick = { video ->
                        navController.navigate(AppDestinations.detailRoute(video))
                    }
                )
            }
            
            composable(AppDestinations.SHORTS) {
                ShortsRoute(
                    onFullscreenChanged = { isFullscreen ->
                        shortsFullscreen = isFullscreen
                    }
                )
            }
            
            composable(
                route = AppDestinations.DETAIL,
                arguments = listOf(
                    navArgument("videoId") { type = NavType.IntType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("coverUrl") { type = NavType.StringType },
                    navArgument("playUrl") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                    navArgument("authorName") { type = NavType.StringType },
                    navArgument("authorIcon") { type = NavType.StringType },
                    navArgument("duration") { type = NavType.IntType },
                    navArgument("description") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val args = backStackEntry.arguments!!
                val video = EyepetizerFeedItem.Video(
                    id = args.getInt("videoId"),
                    title = decode(args.getString("title") ?: ""),
                    description = decode(args.getString("description") ?: ""),
                    coverUrl = decode(args.getString("coverUrl") ?: ""),
                    playUrl = decode(args.getString("playUrl") ?: ""),
                    category = decode(args.getString("category") ?: ""),
                    authorName = decode(args.getString("authorName") ?: ""),
                    authorIcon = decode(args.getString("authorIcon") ?: ""),
                    duration = args.getInt("duration")
                )
                VideoDetailScreen(
                    video = video,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun decode(s: String) = URLDecoder.decode(s, "UTF-8")
