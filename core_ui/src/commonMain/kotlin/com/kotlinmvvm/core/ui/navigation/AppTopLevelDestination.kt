package com.kotlinmvvm.core.ui.navigation

import com.kotlinmvvm.core.navigation.AppRoute

/**
 * @author 浩楠
 * @date 2026/7/24 11:55
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 各端共享的顶层目的地唯一配置，统一路由、标签与排列顺序
 */
enum class AppTopLevelDestination(
    val route: AppRoute.TopLevel,
    val label: String
) {
    HOME(AppRoute.TopLevel.HOME, "首页"),
    SHORTS(AppRoute.TopLevel.SHORTS, "短视频");

    companion object {
        fun from(route: AppRoute.TopLevel?): AppTopLevelDestination {
            return entries.firstOrNull { destination -> destination.route == route } ?: HOME
        }
    }
}
