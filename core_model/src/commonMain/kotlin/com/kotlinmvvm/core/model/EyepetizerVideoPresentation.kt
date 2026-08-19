package com.kotlinmvvm.core.model

/**
 * @author 浩楠
 *
 * @date 2026-3-11
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: KMP 共享视频展示文案格式化
 */
fun EyepetizerFeedItem.Video.authorHandle(): String {
    return "@$authorName"
}

fun EyepetizerFeedItem.Video.categoryTag(): String {
    return "#$category"
}

fun EyepetizerFeedItem.Video.authorCategoryLabel(): String {
    return "$authorName / ${categoryTag()}"
}

fun EyepetizerFeedItem.Video.categoryDurationLabel(): String {
    return "${categoryTag()} · ${duration.toVideoDurationLabel()}"
}

fun Int.toVideoDurationLabel(): String {
    val minutes = this / 60
    val seconds = this % 60
    val paddedSeconds = if (seconds < 10) "0$seconds" else seconds.toString()
    return "$minutes:$paddedSeconds"
}
