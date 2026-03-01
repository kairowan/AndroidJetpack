package com.kotlinmvvm.core.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * @author 浩楠
 *
 * @date 2026-2-26
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

/**
 * 短视频数据接口
 */
interface ShortsItem {
    val id: Any
    val videoUrl: String
}

/**
 * 短视频 Pager
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : ShortsItem> ShortsPager(
    items: List<T>,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState { items.size },
    player: IPlayer = rememberPlayer(),
    preloadCount: Int = 2,
    onPageChanged: (Int) -> Unit = {},
    overlay: @Composable BoxScope.(T, Boolean) -> Unit = { _, _ -> }
) {
    // 当前页变化时播放对应视频
    LaunchedEffect(pagerState.currentPage, items) {
        val page = pagerState.currentPage
        onPageChanged(page)
        if (items.isNotEmpty() && page < items.size) {
            player.play(items[page].videoUrl)

            if (preloadCount > 0) {
                val preloadUrls = (1..preloadCount)
                    .mapNotNull { offset -> items.getOrNull(page + offset)?.videoUrl }
                player.preload(preloadUrls)
            }
        }
    }
    
    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        val item = items[page]
        val isCurrent = pagerState.currentPage == page
        
        ShortsPage(
            item = item,
            player = player,
            isCurrent = isCurrent,
            overlay = { overlay(item, isCurrent) }
        )
    }
}

@Composable
private fun <T : ShortsItem> ShortsPage(
    item: T,
    player: IPlayer,
    isCurrent: Boolean,
    overlay: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { player.toggle() }
                )
            }
    ) {
        // 只有当前页渲染视频
        if (isCurrent) {
            PlayerSurfaceFullscreen(player = player)
        }
        
        overlay()
    }
}

/**
 * 底部渐变遮罩
 */
@Composable
fun ShortsOverlay(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                )
            )
            .padding(16.dp)
    ) {
        Column(content = content)
    }
}
