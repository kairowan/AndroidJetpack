package com.kotlinmvvm.core.player.planning

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
 * @Description: KMP 共享短视频预加载规划器
 */
object ShortsPreloadPlanner {
    fun <T> planUrls(
        items: List<T>,
        currentPage: Int,
        preloadCount: Int,
        videoUrlOf: (T) -> String
    ): List<String> {
        if (preloadCount <= 0 || items.isEmpty()) return emptyList()

        val start = currentPage + 1
        if (start !in items.indices) return emptyList()

        val endExclusive = (start + preloadCount).coerceAtMost(items.size)
        return items.subList(start, endExclusive).map(videoUrlOf)
    }
}
