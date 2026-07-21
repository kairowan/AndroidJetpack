package com.kotlinmvvm.app.navigation.destination

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.kotlinmvvm.app.navigation.model.HomeDestination
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.feature.home.navigation.HomeRoute
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver

/**
 * @author 浩楠
 * @date 2026/7/21 09:23
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 首页目的地装配入口，只负责把应用导航参数和 Feed 分页角色传给 HomeRoute
 */
internal fun EntryProviderScope<NavKey>.registerHomeDestination(
    pageRepository: FeedPageRepository,
    taskObserver: ViewModelTaskObserver,
    onVideoClick: (videoId: Int, source: FeedSource) -> Unit
) {
    entry<HomeDestination> {
        HomeRoute(
            pageRepository = pageRepository,
            taskObserver = taskObserver,
            onVideoClick = { video, source -> onVideoClick(video.id, source) }
        )
    }
}
