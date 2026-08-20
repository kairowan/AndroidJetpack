package com.kotlinmvvm.data.feed.remote

import com.kotlinmvvm.core.network.datasource.NetworkDataSource
import com.kotlinmvvm.data.feed.remote.service.FeedApiService

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Feed 远程数据源实现，把业务接口调用委托给可复用的 NetworkDataSource
 */
internal class RetrofitFeedRemoteDataSource(
    private val service: FeedApiService,
    private val networkDataSource: NetworkDataSource
) : FeedRemoteDataSource {
    override suspend fun fetchSelected() = networkDataSource.execute(service::selected)
    override suspend fun fetchDiscovery() = networkDataSource.execute(service::discovery)
    override suspend fun fetchFollow() = networkDataSource.execute(service::follow)
    override suspend fun fetchHot() = networkDataSource.execute(service::discoveryHot)
    override suspend fun fetchCategories() = networkDataSource.execute(service::discoveryCategory)
    override suspend fun fetchAuthors() = networkDataSource.execute(service::authors)
    override suspend fun fetchNextPage(url: String) =
        networkDataSource.execute { service.nextPage(url) }
}
