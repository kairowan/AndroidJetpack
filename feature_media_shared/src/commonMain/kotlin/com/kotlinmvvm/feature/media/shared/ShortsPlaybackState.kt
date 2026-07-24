package com.kotlinmvvm.feature.media.shared

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Shorts 当前页和全屏模式的跨端不可变状态
 */
data class ShortsPlaybackState(
    val fullscreenMode: FullscreenMode = FullscreenMode.NONE,
    val currentPage: Int = 0
) {
    val isFullscreen: Boolean
        get() = fullscreenMode != FullscreenMode.NONE

    fun normalizedCurrentPage(totalCount: Int): Int {
        if (totalCount <= 0) return 0
        return currentPage.coerceIn(0, totalCount - 1)
    }
}
