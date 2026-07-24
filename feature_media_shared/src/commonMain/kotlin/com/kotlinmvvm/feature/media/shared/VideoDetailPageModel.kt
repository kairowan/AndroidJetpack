package com.kotlinmvvm.feature.media.shared

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 详情页 UI 在不同平台消费的纯展示快照
 */
data class VideoDetailPageModel(
    val title: String,
    val playUrl: String,
    val metadataLabel: String,
    val authorName: String,
    val authorIcon: String,
    val descriptionText: String,
    val isFullscreen: Boolean,
    val isLandscapeFullscreen: Boolean,
    val controlsCopy: VideoDetailControlCopy
)
