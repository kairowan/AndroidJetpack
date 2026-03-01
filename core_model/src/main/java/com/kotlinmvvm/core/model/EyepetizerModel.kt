package com.kotlinmvvm.core.model

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
