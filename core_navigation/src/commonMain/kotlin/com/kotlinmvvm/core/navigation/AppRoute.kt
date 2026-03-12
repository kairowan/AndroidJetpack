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
 * @Description: App 壳层共享路由协议，统一描述顶层页面与详情页目标
 */
sealed interface AppRoute {
    val topLevelRoute: TopLevel?
        get() = when (this) {
            Home -> TopLevel.HOME
            Shorts -> TopLevel.SHORTS
            is Detail -> null
        }

    data object Home : AppRoute

    data object Shorts : AppRoute

    data class Detail(val video: EyepetizerFeedItem.Video) : AppRoute

    enum class TopLevel {
        HOME,
        SHORTS
    }
}
