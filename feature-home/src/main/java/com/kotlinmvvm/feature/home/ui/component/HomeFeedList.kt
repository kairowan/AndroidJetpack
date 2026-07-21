package com.kotlinmvvm.feature.home.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kotlinmvvm.core.designsystem.theme.AppSpacing
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedTextFooter
import com.kotlinmvvm.domain.feed.model.FeedTextHeader
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.feature.home.R

/**
 * @author 浩楠
 * @date 2026/7/20 14:42
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 首页专用下拉刷新分页列表，负责尾部触发、稳定键和分页状态展示
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedList(
    items: List<FeedItem>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    itemContent: @Composable (FeedItem) -> Unit
) {
    val shouldLoadMore by remember(listState, items.size) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?.let { it >= items.lastIndex - 2 } == true
        }
    }

    // 分页状态不作为 Effect 键，避免失败复位后在同一列表位置立即重复请求。
    LaunchedEffect(shouldLoadMore, items.size, canLoadMore) {
        if (shouldLoadMore && canLoadMore && !isLoadingMore) onLoadMore()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = AppSpacing.small)
        ) {
            items(
                count = items.size,
                key = { index -> items[index].stableKey(index) }
            ) { index -> itemContent(items[index]) }

            if (isLoadingMore) {
                item(key = "loading_more") {
                    Box(
                        Modifier.fillMaxWidth().padding(AppSpacing.large),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(Modifier.size(AppSpacing.extraLarge)) }
                }
            }
            if (!canLoadMore && items.isNotEmpty()) {
                item(key = "no_more") {
                    Box(
                        Modifier.fillMaxWidth().padding(AppSpacing.large),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.home_no_more),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun FeedItem.stableKey(index: Int): String = when (this) {
    is FeedVideo -> "video_$id"
    is FeedTextHeader -> "header_${index}_${text.hashCode()}"
    is FeedTextFooter -> "footer_${index}_${text.hashCode()}"
}
