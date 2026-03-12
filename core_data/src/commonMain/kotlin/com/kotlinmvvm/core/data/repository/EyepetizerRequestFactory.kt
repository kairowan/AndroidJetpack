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
        return EyepetizerRequest(
            source = source,
            url = nextPageUrl.toAbsoluteUrlOrNull() ?: (EYEPETIZER_BASE_URL + source.path)
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
        val normalizedUrl = when {
            startsWith(EYEPETIZER_HTTP_HOST_PREFIX) ->
                EYEPETIZER_HTTPS_HOST_PREFIX + removePrefix(EYEPETIZER_HTTP_HOST_PREFIX)

            else -> this
        }
        if (normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://")) {
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
 * @Description: Eyepetizer 请求快照
 */
internal data class EyepetizerRequest(
    val source: EyepetizerFeedSource,
    val url: String
)
