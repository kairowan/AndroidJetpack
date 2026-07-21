package com.kotlinmvvm.app.navigation.destination

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.kotlinmvvm.app.navigation.model.ShortsDestination
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.feature.shorts.navigation.ShortsRoute
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 短视频目的地装配入口，连接分页角色、全屏反馈和 Activity 窗口策略
 */
internal fun EntryProviderScope<NavKey>.registerShortsDestination(
    pageRepository: FeedPageRepository,
    videoPlayerFactory: VideoPlayerFactory,
    taskObserver: ViewModelTaskObserver,
    onFullscreenChanged: (Boolean) -> Unit,
    onWindowModeChanged: (VideoWindowMode) -> Unit
) {
    entry<ShortsDestination> {
        ShortsRoute(
            pageRepository = pageRepository,
            videoPlayerFactory = videoPlayerFactory,
            taskObserver = taskObserver,
            onFullscreenChanged = onFullscreenChanged,
            onWindowModeChanged = onWindowModeChanged
        )
    }
}
