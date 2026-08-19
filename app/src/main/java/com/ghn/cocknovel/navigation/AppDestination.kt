package com.ghn.cocknovel.navigation

import androidx.navigation3.runtime.NavKey
import com.kotlinmvvm.core.navigation.AppRoute
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import kotlinx.serialization.Serializable

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
 * 描述: App 模块的 Navigation3 目标与共享路由映射
 */
@Serializable
internal sealed interface AppDestination : NavKey

internal fun AppRoute.TopLevel.toDestination(): AppDestination {
    return when (this) {
        AppRoute.TopLevel.HOME -> HomeDestination
        AppRoute.TopLevel.SHORTS -> ShortsDestination
    }
}

internal fun AppRoute.toDestination(): AppDestination {
    return when (this) {
        AppRoute.Home -> HomeDestination
        AppRoute.Shorts -> ShortsDestination
        is AppRoute.Detail -> video.toDestination()
    }
}

internal fun AppDestination.toAppRoute(): AppRoute {
    return when (this) {
        HomeDestination -> AppRoute.Home
        ShortsDestination -> AppRoute.Shorts
        is DetailDestination -> AppRoute.Detail(toVideo())
    }
}

internal fun EyepetizerFeedItem.Video.toDestination(): DetailDestination {
    return DetailDestination(
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

private fun DetailDestination.toVideo(): EyepetizerFeedItem.Video {
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
