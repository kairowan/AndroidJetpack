package com.kotlinmvvm.data.feed.di

import com.kotlinmvvm.core.network.client.NetworkClientFactory
import com.kotlinmvvm.core.network.config.NetworkEndpoint
import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy
import com.kotlinmvvm.core.network.datasource.RetrofitNetworkDataSource
import com.kotlinmvvm.core.network.observer.NetworkFailureObserver
import com.kotlinmvvm.data.feed.local.FileFeedLocalDataSource
import com.kotlinmvvm.data.feed.remote.RetrofitFeedRemoteDataSource
import com.kotlinmvvm.data.feed.remote.service.FeedApiService
import com.kotlinmvvm.data.feed.repository.DefaultFeedRepository
import com.kotlinmvvm.data.feed.observer.FeedDataObserver
import com.kotlinmvvm.data.feed.observer.NoOpFeedDataObserver
import java.io.File

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Feed 数据图装配入口，在数据模块内部封装 Service、数据源和仓库实现的构造细节
 */
fun createFeedDataBindings(
    networkClientFactory: NetworkClientFactory,
    endpoint: NetworkEndpoint,
    remoteResourceUrlPolicy: RemoteResourceUrlPolicy,
    cacheDirectory: File,
    networkFailureObserver: NetworkFailureObserver = NetworkFailureObserver.None,
    dataObserver: FeedDataObserver = NoOpFeedDataObserver
): FeedDataBindings {
    val repository = DefaultFeedRepository(
        remoteDataSource = RetrofitFeedRemoteDataSource(
            service = networkClientFactory.createService(endpoint, FeedApiService::class.java),
            networkDataSource = RetrofitNetworkDataSource(
                failureObserver = networkFailureObserver
            )
        ),
        localDataSource = FileFeedLocalDataSource(cacheDirectory),
        remoteResourceUrlPolicy = remoteResourceUrlPolicy,
        dataObserver = dataObserver
    )
    return FeedDataBindings(
        pageRepository = repository,
        videoRepository = repository
    )
}
