package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.model.EyepetizerFeedSource

internal const val EYEPETIZER_BASE_URL: String = "https://baobab.kaiyanapp.com/"
private const val EYEPETIZER_HOST: String = "baobab.kaiyanapp.com"
private const val EYEPETIZER_HTTP_HOST_PREFIX: String = "http://$EYEPETIZER_HOST/"
private const val EYEPETIZER_HTTPS_HOST_PREFIX: String = "https://$EYEPETIZER_HOST/"

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
 * @Description: Eyepetizer 请求 URL 构造器
 */
internal object EyepetizerRequestFactory {
    fun create(
        source: EyepetizerFeedSource,
        nextPageUrl: String?
    ): EyepetizerRequest {
        val requestUrl = if (nextPageUrl.isNullOrBlank()) {
            EYEPETIZER_BASE_URL + source.path
        } else {
            nextPageUrl.toAbsoluteUrlOrNull()
                ?: throw EyepetizerInvalidUrlException(nextPageUrl)
        }
        return EyepetizerRequest(
            source = source,
            url = requestUrl
        )
    }

    private val EyepetizerFeedSource.path: String
        get() = when (this) {
            EyepetizerFeedSource.HOME_SELECTED -> "api/v4/tabs/selected"
            EyepetizerFeedSource.DISCOVERY -> "api/v4/discovery"
            EyepetizerFeedSource.FOLLOW -> "api/v4/tabs/follow"
            EyepetizerFeedSource.DISCOVERY_HOT -> "api/v4/discovery/hot"
            EyepetizerFeedSource.DISCOVERY_CATEGORY -> "api/v4/discovery/category"
            EyepetizerFeedSource.PGCS_ALL -> "api/v4/pgcs/all"
        }

    private fun String?.toAbsoluteUrlOrNull(): String? {
        if (this.isNullOrBlank()) return null
        val candidate = trim()
        val normalizedUrl = when {
            candidate.startsWith(EYEPETIZER_HTTP_HOST_PREFIX) ->
                EYEPETIZER_HTTPS_HOST_PREFIX + candidate.removePrefix(EYEPETIZER_HTTP_HOST_PREFIX)
            candidate.startsWith(EYEPETIZER_HTTPS_HOST_PREFIX) -> candidate
            candidate.startsWith("//") ||
                candidate.contains('\\') ||
                candidate.contains("://") -> return null
            else -> candidate
        }
        if (normalizedUrl.startsWith(EYEPETIZER_HTTPS_HOST_PREFIX)) {
            return normalizedUrl
        }
        val normalizedBaseUrl = EYEPETIZER_BASE_URL.removeSuffix("/")
        return if (normalizedUrl.startsWith("/")) {
            normalizedBaseUrl + normalizedUrl
        } else {
            "$normalizedBaseUrl/$normalizedUrl"
        }
    }
}
