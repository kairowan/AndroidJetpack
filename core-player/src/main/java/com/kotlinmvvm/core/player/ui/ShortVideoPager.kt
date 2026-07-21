package com.kotlinmvvm.core.player.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.defaults.ShortVideoPagerDefaults
import com.kotlinmvvm.core.player.model.ShortVideoItem

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用竖向短视频分页器，播放稳定页并按配置预加载后续视频
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : ShortVideoItem> ShortVideoPager(
    items: List<T>,
    player: VideoPlayerController,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState { items.size },
    preloadCount: Int = ShortVideoPagerDefaults.PRELOAD_COUNT,
    onPageChanged: (Int) -> Unit = {},
    overlay: @Composable BoxScope.(T, Boolean) -> Unit = { _, _ -> }
) {
    val currentPage = pagerState.settledPage
    val currentItem = items.getOrNull(currentPage)

    LaunchedEffect(currentPage, currentItem?.videoUrl, items.size, preloadCount) {
        onPageChanged(currentPage)
        if (currentItem == null) return@LaunchedEffect

        player.play(currentItem.videoUrl)

        val urlsToPreload = items.preloadUrlsFrom(
            page = currentPage,
            count = preloadCount
        )
        if (urlsToPreload.isNotEmpty()) {
            player.preload(urlsToPreload)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        key = { index -> items[index].id }
    ) { page ->
        val item = items[page]
        ShortsPage(
            player = player,
            isCurrent = page == currentPage
        ) {
            overlay(item, page == currentPage)
        }
    }
}

private fun <T : ShortVideoItem> List<T>.preloadUrlsFrom(
    page: Int,
    count: Int
): List<String> {
    if (count <= 0 || isEmpty()) return emptyList()

    val start = page + 1
    if (start !in indices) return emptyList()

    val endExclusive = (start + count).coerceAtMost(size)
    return subList(start, endExclusive).map { it.videoUrl }
}

@Composable
private fun ShortsPage(
    player: VideoPlayerController,
    isCurrent: Boolean,
    overlay: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(player) {
                detectTapGestures(onDoubleTap = { player.toggle() })
            }
    ) {
        if (isCurrent) {
            PlayerSurface(player = player, modifier = Modifier.fillMaxSize())
        }
        overlay()
    }
}
