package com.kotlinmvvm.app.di

import com.kotlinmvvm.app.config.AppNavigationMode
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.domain.feed.repository.FeedVideoRepository
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import coil.ImageLoader

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用层稳定依赖契约，使手动容器或大型项目 DI 实现都能向 Activity 暴露同一组领域角色
 */
interface AppDependencies {
    /** 当前构建使用的单 Activity 或多 Activity 页面宿主模式。 */
    val navigationMode: AppNavigationMode

    /** 为信息流列表、刷新与分页页面提供的进程级仓库角色。 */
    val feedPageRepository: FeedPageRepository

    /** 为视频详情恢复与观察提供的进程级仓库角色。 */
    val feedVideoRepository: FeedVideoRepository

    /** 统一使用媒体 Host 白名单的进程级图片加载器。 */
    val imageLoader: ImageLoader

    /** 为详情和短视频页面创建具备受控传输、缓存和生命周期能力的播放器。 */
    val videoPlayerFactory: VideoPlayerFactory

    /** 页面异步任务观察器，小型项目可空实现，大型项目可由宿主接入统一监控。 */
    val viewModelTaskObserver: ViewModelTaskObserver
}
