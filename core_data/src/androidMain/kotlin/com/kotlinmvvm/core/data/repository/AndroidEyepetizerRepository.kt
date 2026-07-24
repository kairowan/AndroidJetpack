package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.data.eyepetizer.toDomainFeedPage
import com.kotlinmvvm.core.data.network.ApiServiceFactory
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author 浩楠
 *
 * @date 2026-3-9
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Android 侧 Eyepetizer 仓库实现，并保留协程取消语义
 */
internal class AndroidEyepetizerRepository : FeedPageRepository {
    private val apiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        ApiServiceFactory.createApiService()
    }

    override suspend fun loadPage(
        source: EyepetizerFeedSource,
        continuationToken: String?
    ): Result<FeedPage> = withContext(Dispatchers.IO) {
        try {
            val response = if (continuationToken.isNullOrEmpty()) {
                when (source) {
                    EyepetizerFeedSource.HOME_SELECTED -> apiService.getEyepetizerHome()
                    EyepetizerFeedSource.DISCOVERY -> apiService.getEyepetizerDiscovery()
                    EyepetizerFeedSource.FOLLOW -> apiService.getEyepetizerFollow()
                    EyepetizerFeedSource.DISCOVERY_HOT -> apiService.getEyepetizerDiscoveryHot()
                    EyepetizerFeedSource.DISCOVERY_CATEGORY -> apiService.getEyepetizerDiscoveryCategory()
                    EyepetizerFeedSource.PGCS_ALL -> apiService.getEyepetizerPgcsAll()
                }
            } else {
                apiService.getEyepetizerHomeMore(continuationToken)
            }

            Result.success(response.toDomainFeedPage())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
