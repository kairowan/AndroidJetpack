package com.ghn.cocknovel.navigation

import androidx.navigation3.runtime.NavKey
import com.kotlinmvvm.core.navigation.AppRoute
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import kotlinx.serialization.Serializable

/**
 * @author 浩楠
 *
 * @date 2026-3-9
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: App 模块的 Navigation3 目标与共享路由映射
 */
@Serializable
internal sealed interface AppDestination : NavKey {
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

internal fun AppRoute.TopLevel.toDestination(): AppDestination {
    return when (this) {
        AppRoute.TopLevel.HOME -> AppDestination.Home
        AppRoute.TopLevel.SHORTS -> AppDestination.Shorts
    }
}

internal fun AppRoute.toDestination(): AppDestination {
    return when (this) {
        AppRoute.Home -> AppDestination.Home
        AppRoute.Shorts -> AppDestination.Shorts
        is AppRoute.Detail -> video.toDestination()
    }
}

internal fun AppDestination.toAppRoute(): AppRoute {
    return when (this) {
        AppDestination.Home -> AppRoute.Home
        AppDestination.Shorts -> AppRoute.Shorts
        is AppDestination.Detail -> AppRoute.Detail(toVideo())
    }
}

internal fun EyepetizerFeedItem.Video.toDestination(): AppDestination.Detail {
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
