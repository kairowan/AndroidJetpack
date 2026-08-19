package com.kotlinmvvm.core.navigation

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
 * @Description: App 壳层共享状态，统一协调底栏显示与 Shorts 离场行为
 */
data class AppShellState(
    val isShortsFullscreen: Boolean = false,
    val shortsDeactivateSignal: Int = 0
) {
    fun shouldShowBottomBar(currentRoute: AppRoute?): Boolean {
        return currentRoute is AppRoute.Home ||
            (currentRoute is AppRoute.Shorts && !isShortsFullscreen)
    }

    fun usesDarkNavigationChrome(currentRoute: AppRoute?): Boolean {
        return currentRoute is AppRoute.Shorts ||
            currentRoute is AppRoute.Detail
    }

    fun onTopLevelNavigation(
        currentRoute: AppRoute?,
        targetRoute: AppRoute.TopLevel
    ): AppShellState {
        if (currentRoute?.topLevelRoute == targetRoute) {
            return this
        }
        return if (currentRoute is AppRoute.Shorts && targetRoute != AppRoute.TopLevel.SHORTS) {
            copy(
                isShortsFullscreen = false,
                shortsDeactivateSignal = shortsDeactivateSignal + 1
            )
        } else if (targetRoute != AppRoute.TopLevel.SHORTS) {
            copy(isShortsFullscreen = false)
        } else {
            this
        }
    }

    fun onDestinationChanged(route: AppRoute?): AppShellState {
        return when (route) {
            is AppRoute.Shorts -> this
            else -> copy(isShortsFullscreen = false)
        }
    }

    fun onShortsFullscreenChanged(isFullscreen: Boolean): AppShellState {
        return copy(isShortsFullscreen = isFullscreen)
    }
}
