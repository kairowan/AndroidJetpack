package com.ghn.cocknovel.di

import android.content.Context
import com.kotlinmvvm.core.data.repository.EyepetizerRepositoryFactory
import com.kotlinmvvm.core.network.NetworkConfig
import com.kotlinmvvm.core.network.NetworkRetryPolicy
import com.kotlinmvvm.core.network.createAndroidNetworkClient
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.core.player.facade.createVideoPlayerFactory
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository

/**
 * @author 浩楠
 * @date 2026/7/24 11:53
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Android 进程级依赖装配入口
 */
internal class AppContainer(
    context: Context
) : AppDependencies {
    private val appContext = context.applicationContext

    private val networkClient = createAndroidNetworkClient(
        context = appContext,
        config = NetworkConfig(
            retryPolicy = NetworkRetryPolicy(maxRetries = 1)
        )
    )

    override val eyepetizerRepository: FeedPageRepository by lazy(
        LazyThreadSafetyMode.SYNCHRONIZED
    ) {
        EyepetizerRepositoryFactory.create(networkClient)
    }

    override val videoPlayerFactory: VideoPlayerFactory by lazy(
        LazyThreadSafetyMode.SYNCHRONIZED
    ) {
        createVideoPlayerFactory(appContext)
    }
}
