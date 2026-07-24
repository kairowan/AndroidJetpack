package com.kotlinmvvm.feature.media.shared

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Shorts 纯 CMP Screen 的完整不可变输入
 */
data class ShortsPageModel(
    val isLoading: Boolean,
    val errorMessage: String?,
    val emptyMessage: String,
    val videos: List<ShortsVideoCardModel>,
    val currentPage: Int,
    val isFullscreen: Boolean,
    val isLandscapeFullscreen: Boolean,
    val isLoadingMore: Boolean,
    val canLoadMore: Boolean,
    val controlsCopy: ShortsControlCopy
)
