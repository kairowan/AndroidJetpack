package com.kotlinmvvm.app.navigation.destination

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.kotlinmvvm.app.navigation.model.VideoDetailDestination
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.domain.feed.repository.FeedVideoRepository
import com.kotlinmvvm.feature.detail.navigation.VideoDetailRoute
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情目的地装配入口，连接类型安全路由参数、详情仓库角色与宿主窗口回调
 */
internal fun EntryProviderScope<NavKey>.registerVideoDetailDestination(
    videoRepository: FeedVideoRepository,
    videoPlayerFactory: VideoPlayerFactory,
    taskObserver: ViewModelTaskObserver,
    onBack: () -> Unit,
    onWindowModeChanged: (VideoWindowMode) -> Unit
) {
    entry<VideoDetailDestination> { destination ->
        VideoDetailRoute(
            videoId = destination.videoId,
            source = destination.source,
            videoRepository = videoRepository,
            videoPlayerFactory = videoPlayerFactory,
            taskObserver = taskObserver,
            onBack = onBack,
            onWindowModeChanged = onWindowModeChanged
        )
    }
}
