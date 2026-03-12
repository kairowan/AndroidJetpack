package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.model.EyepetizerFeedSource

internal const val EYEPETIZER_BASE_URL: String = "http://baobab.kaiyanapp.com/"

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
        if (startsWith("http://") || startsWith("https://")) {
            return this
        }
        val normalizedBaseUrl = EYEPETIZER_BASE_URL.removeSuffix("/")
        return if (startsWith("/")) {
            normalizedBaseUrl + this
        } else {
            "$normalizedBaseUrl/$this"
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
