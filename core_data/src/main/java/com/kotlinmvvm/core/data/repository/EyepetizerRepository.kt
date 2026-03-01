package com.kotlinmvvm.core.data.repository

import com.kt.network.net.ApiService
import com.kt.network.net.RetrofitClient
import com.kotlinmvvm.core.model.EyepetizerFeed
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author 浩楠
 *
 * @date 2026-2-25
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

class EyepetizerRepository {
    
    private val apiService: ApiService by lazy {
        RetrofitClient.getInstance(null).getDefault(ApiService::class.java, 7)
    }

    suspend fun getHomeFeed(nextPageUrl: String? = null): Result<EyepetizerFeed> = withContext(Dispatchers.IO) {
        try {
            val response = if (nextPageUrl.isNullOrEmpty()) {
                apiService.getEyepetizerHome()
            } else {
                apiService.getEyepetizerHomeMore(nextPageUrl)
            }
            
            val items = response.itemList?.mapNotNull { item ->
                when (item.type) {
                    "video" -> {
                        val data = item.data
                        val id = data?.id
                        if (data != null && id != null) {
                            EyepetizerFeedItem.Video(
                                id = id,
                                title = data.title ?: "",
                                description = data.description ?: "",
                                coverUrl = data.cover?.feed ?: "",
                                playUrl = data.playUrl ?: "",
                                category = data.category ?: "",
                                authorName = data.author?.name ?: "",
                                authorIcon = data.author?.icon ?: "",
                                duration = data.duration ?: 0
                            )
                        } else null
                    }
                    "textHeader" -> EyepetizerFeedItem.TextHeader(item.data?.text ?: "")
                    "textFooter" -> EyepetizerFeedItem.TextFooter(item.data?.text ?: "")
                    else -> null
                }
            } ?: emptyList()

            Result.success(EyepetizerFeed(items, response.nextPageUrl))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
