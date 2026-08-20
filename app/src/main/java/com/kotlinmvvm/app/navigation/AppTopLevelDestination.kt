package com.kotlinmvvm.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import com.kotlinmvvm.app.R
import com.kotlinmvvm.app.navigation.model.AppRoute
import com.kotlinmvvm.app.navigation.model.HomeDestination
import com.kotlinmvvm.app.navigation.model.ShortsDestination

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用顶层导航唯一配置表，为返回栈、底部导航和宽屏导航提供同一份路由元数据
 */
internal enum class AppTopLevelDestination(
    val route: AppRoute,
    @param:StringRes val labelResId: Int,
    val icon: ImageVector
) {
    HOME(HomeDestination, R.string.navigation_home, Icons.Default.Home),
    SHORTS(ShortsDestination, R.string.navigation_shorts, Icons.Default.PlayArrow)
}
