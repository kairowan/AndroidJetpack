package com.kotlinmvvm.core.navigation

import com.kotlinmvvm.core.model.EyepetizerFeedItem

/**
 * @author 浩楠
 *
 * @date 2026-3-11
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: App 共享导航状态，统一维护 route back stack 与壳层状态
 */
data class AppNavigationState(
    val backStack: List<AppRoute> = listOf(AppRoute.Home),
    val shellState: AppShellState = AppShellState()
) {
    val currentRoute: AppRoute
        get() = backStack.lastOrNull() ?: AppRoute.Home

    val shortsDeactivateSignal: Int
        get() = shellState.shortsDeactivateSignal

    fun shouldShowBottomBar(): Boolean {
        return shellState.shouldShowBottomBar(currentRoute)
    }

    fun usesDarkNavigationChrome(): Boolean {
        return shellState.usesDarkNavigationChrome(currentRoute)
    }

    fun navigateToTopLevel(targetRoute: AppRoute.TopLevel): AppNavigationState {
        val updatedShellState = shellState.onTopLevelNavigation(
            currentRoute = currentRoute,
            targetRoute = targetRoute
        )
        if (currentRoute.topLevelRoute == targetRoute) {
            return copy(shellState = updatedShellState)
        }
        val target = when (targetRoute) {
            AppRoute.TopLevel.HOME -> AppRoute.Home
            AppRoute.TopLevel.SHORTS -> AppRoute.Shorts
        }
        return copy(
            backStack = listOf(target),
            shellState = updatedShellState.onDestinationChanged(target)
        )
    }

    fun openDetail(video: EyepetizerFeedItem.Video): AppNavigationState {
        val detailRoute = AppRoute.Detail(video)
        return copy(
            backStack = backStack.normalized() + detailRoute,
            shellState = shellState.onDestinationChanged(detailRoute)
        )
    }

    fun pop(): AppNavigationState {
        val normalizedBackStack = backStack.normalized()
        if (normalizedBackStack.size <= 1) {
            return copy(backStack = normalizedBackStack)
        }
        val updatedBackStack = normalizedBackStack.dropLast(1)
        val updatedCurrentRoute = updatedBackStack.last()
        return copy(
            backStack = updatedBackStack,
            shellState = shellState.onDestinationChanged(updatedCurrentRoute)
        )
    }

    fun onShortsFullscreenChanged(isFullscreen: Boolean): AppNavigationState {
        return copy(shellState = shellState.onShortsFullscreenChanged(isFullscreen))
    }

    companion object {
        fun fromBackStack(
            backStack: List<AppRoute>,
            shellState: AppShellState = AppShellState()
        ): AppNavigationState {
            return AppNavigationState(
                backStack = backStack.normalized(),
                shellState = shellState.onDestinationChanged(backStack.normalized().last())
            )
        }
    }
}

private fun List<AppRoute>.normalized(): List<AppRoute> {
    return if (isEmpty()) listOf(AppRoute.Home) else this
}
