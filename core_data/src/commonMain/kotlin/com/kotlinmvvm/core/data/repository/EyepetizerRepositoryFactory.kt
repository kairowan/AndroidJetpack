package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.data.eyepetizer.EyepetizerPayloadResponse
import com.kotlinmvvm.core.data.eyepetizer.toDomainFeedPage
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.core.network.NetworkClient
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

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
 * @Description: 使用统一 NetworkClient 的跨平台仓库创建入口
 */
object EyepetizerRepositoryFactory {
    fun create(networkClient: NetworkClient): FeedPageRepository =
        EyepetizerRepository(networkClient)
}

private class EyepetizerRepository(
    private val networkClient: NetworkClient
) : FeedPageRepository {
    override suspend fun loadPage(
        source: EyepetizerFeedSource,
        continuationToken: String?
    ): Result<FeedPage> {
        val request = try {
            EyepetizerRequestFactory.create(
                source = source,
                nextPageUrl = continuationToken
            )
        } catch (error: Exception) {
            return Result.failure(error)
        }
        return try {
            Result.success(
                decodeEyepetizerPage(
                    payloadText = networkClient.getText(request.url),
                    requestUrl = request.url
                )
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}

internal fun decodeEyepetizerPage(
    payloadText: String,
    requestUrl: String
): FeedPage {
    val payload = try {
        PAYLOAD_JSON.decodeFromString<EyepetizerPayloadResponse>(payloadText)
    } catch (error: SerializationException) {
        throw EyepetizerPayloadParseException(
            detail = "invalid JSON ($requestUrl)",
            cause = error
        )
    }
    return payload.toDomainFeedPage()
}

private val PAYLOAD_JSON = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}
