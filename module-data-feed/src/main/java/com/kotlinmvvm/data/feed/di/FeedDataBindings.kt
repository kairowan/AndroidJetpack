package com.kotlinmvvm.data.feed.di

import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.domain.feed.repository.FeedVideoRepository

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Feed 数据模块公开装配结果，以具名领域角色隐藏共享仓库实现和内部数据链路
 */
class FeedDataBindings internal constructor(
    val pageRepository: FeedPageRepository,
    val videoRepository: FeedVideoRepository
)
