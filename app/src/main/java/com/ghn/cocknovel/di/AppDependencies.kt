package com.ghn.cocknovel.di

import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository

/**
 * @author 浩楠
 * @date 2026/7/24 11:53
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用向页面暴露的最小可替换依赖契约
 */
interface AppDependencies {
    val eyepetizerRepository: FeedPageRepository
    val videoPlayerFactory: VideoPlayerFactory
}
