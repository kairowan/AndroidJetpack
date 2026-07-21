package com.kotlinmvvm.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.kotlinmvvm.app.navigation.model.AppRoute
import com.kotlinmvvm.app.navigation.model.HomeDestination
import com.kotlinmvvm.app.navigation.model.ShortsDestination

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用多返回栈状态持有者，为每个顶层页面保存独立栈并统一处理跳转和返回
 */
@Stable
class AppNavigationState internal constructor(
    private val topLevelRouteState: MutableState<AppRoute>,
    val backStacks: Map<AppRoute, NavBackStack<NavKey>>,
    private val startRoute: AppRoute = HomeDestination
) {
    var topLevelRoute: AppRoute
        get() = topLevelRouteState.value
        private set(value) {
            require(value in backStacks) { "顶层路由未注册返回栈: $value" }
            topLevelRouteState.value = value
        }

    val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(topLevelRoute)

    val currentRoute: AppRoute?
        get() = currentBackStack.lastOrNull() as? AppRoute

    /** 跳转到业务目的地；当前目的地相同时不会重复入栈。 */
    fun navigate(route: AppRoute) {
        if (route in backStacks) {
            navigateTopLevel(route)
        } else if (currentRoute != route) {
            currentBackStack.add(route)
        }
    }

    /** 切换 Home 或 Shorts 顶层目的地，并保留另一个目的地的完整返回栈。 */
    fun navigateTopLevel(route: AppRoute) {
        require(route == HomeDestination || route == ShortsDestination) {
            "顶层路由只允许 Home 或 Shorts"
        }
        if (topLevelRoute != route) topLevelRoute = route
    }

    /** 优先弹出当前子页面，其次回到起始顶层页面；根页面返回 `false`。 */
    fun pop(): Boolean {
        return when {
            currentBackStack.size > 1 -> {
                currentBackStack.removeLastOrNull()
                true
            }

            topLevelRoute != startRoute -> {
                topLevelRoute = startRoute
                true
            }

            else -> false
        }
    }
}

/** 创建可跨配置变化和进程恢复的应用导航状态。 */
@Composable
fun rememberAppNavigationState(): AppNavigationState {
    val topLevelRouteState = rememberSerializable { mutableStateOf<AppRoute>(HomeDestination) }
    val homeBackStack = rememberNavBackStack(HomeDestination)
    val shortsBackStack = rememberNavBackStack(ShortsDestination)
    return remember(topLevelRouteState, homeBackStack, shortsBackStack) {
        AppNavigationState(
            topLevelRouteState = topLevelRouteState,
            backStacks = mapOf(
                HomeDestination to homeBackStack,
                ShortsDestination to shortsBackStack
            )
        )
    }
}
