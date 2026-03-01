package com.kotlinmvvm.core.model

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

data class EyepetizerFeed(
    val items: List<EyepetizerFeedItem>,
    val nextPageUrl: String?
)

sealed class EyepetizerFeedItem {
    data class Video(
        val id: Int,
        val title: String,
        val description: String,
        val coverUrl: String,
        val playUrl: String,
        val category: String,
        val authorName: String,
        val authorIcon: String,
        val duration: Int
    ) : EyepetizerFeedItem()

    data class TextHeader(val text: String) : EyepetizerFeedItem()
    
    data class TextFooter(val text: String) : EyepetizerFeedItem()
}
