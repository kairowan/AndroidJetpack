package com.kotlinmvvm.core.player.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.defaults.ShortsPagerDefaults
import com.kotlinmvvm.core.player.model.ShortsItem
import com.kotlinmvvm.core.player.provider.rememberPlayer

/**
 * 短视频纵向分页播放器。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : ShortsItem> ShortsPager(
    items: List<T>,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState { items.size },
    player: IPlayer = rememberPlayer(),
    preloadCount: Int = ShortsPagerDefaults.PRELOAD_COUNT,
    onPageChanged: (Int) -> Unit = {},
    overlay: @Composable BoxScope.(T, Boolean) -> Unit = { _, _ -> }
) {
    val currentPage = pagerState.currentPage
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

private fun <T : ShortsItem> List<T>.preloadUrlsFrom(
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
    player: IPlayer,
    isCurrent: Boolean,
    overlay: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { player.toggle() })
            }
    ) {
        if (isCurrent) {
            PlayerSurfaceFullscreen(player = player)
        }
        overlay()
    }
}

/**
 * 底部渐变遮罩。
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
                    listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = ShortsPagerDefaults.OVERLAY_GRADIENT_ALPHA)
                    )
                )
            )
            .padding(ShortsPagerDefaults.OVERLAY_PADDING)
    ) {
        Column(content = content)
    }
}
