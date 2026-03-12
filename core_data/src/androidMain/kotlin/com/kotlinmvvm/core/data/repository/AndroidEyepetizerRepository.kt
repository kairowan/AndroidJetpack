package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.data.eyepetizer.toDomainFeed
import com.kotlinmvvm.core.model.EyepetizerFeed
import com.kotlinmvvm.core.model.EyepetizerFeedSource
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
 * @Description: Android 侧 Eyepetizer 仓库实现
 */
internal class AndroidEyepetizerRepository : BaseApiRepository(), EyepetizerRepository {
    override suspend fun getFeed(
        source: EyepetizerFeedSource,
        nextPageUrl: String?
    ): Result<EyepetizerFeed> = withContext(Dispatchers.IO) {
        try {
            val response = if (nextPageUrl.isNullOrEmpty()) {
                when (source) {
                    EyepetizerFeedSource.HOME_SELECTED -> apiService.getEyepetizerHome()
                    EyepetizerFeedSource.DISCOVERY -> apiService.getEyepetizerDiscovery()
                    EyepetizerFeedSource.FOLLOW -> apiService.getEyepetizerFollow()
                    EyepetizerFeedSource.DISCOVERY_HOT -> apiService.getEyepetizerDiscoveryHot()
                    EyepetizerFeedSource.DISCOVERY_CATEGORY -> apiService.getEyepetizerDiscoveryCategory()
                    EyepetizerFeedSource.PGCS_ALL -> apiService.getEyepetizerPgcsAll()
                }
            } else {
                apiService.getEyepetizerHomeMore(nextPageUrl)
            }

            Result.success(response.toDomainFeed())
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
