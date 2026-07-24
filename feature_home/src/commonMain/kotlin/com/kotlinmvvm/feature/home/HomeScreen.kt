package com.kotlinmvvm.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import com.kotlinmvvm.core.state.PagedData
import com.kotlinmvvm.core.ui.component.EmptyContent
import com.kotlinmvvm.core.ui.component.ErrorContent
import com.kotlinmvvm.core.ui.component.LoadingContent
import com.kotlinmvvm.core.ui.component.PagedList
import com.kotlinmvvm.feature.home.shared.HomeFeedCatalog
import com.kotlinmvvm.feature.home.shared.HomeFeedEntryType
import com.kotlinmvvm.feature.home.shared.HomeFeedPageModel
import com.kotlinmvvm.feature.home.shared.HomeVideoCardModel

/**
 * @author 浩楠
 * @date 2026/7/24 11:50
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Android、iOS、Desktop 与 Web 共用的首页纯 UI；平台宿主只注入图片渲染能力
 */
@Composable
fun HomeScreen(
    pageModel: HomeFeedPageModel,
    onSourceSelected: (EyepetizerFeedSource) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onVideoClick: (HomeVideoCardModel) -> Unit,
    image: @Composable (url: String, contentDescription: String, modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeFeedCatalog.sourceOptions.forEach { source ->
                FilterChip(
                    selected = source.key == pageModel.selectedSourceKey,
                    onClick = { onSourceSelected(source.source) },
                    label = { Text(source.title) }
                )
            }
        }

        when {
            pageModel.isLoading && pageModel.entries.isEmpty() -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            pageModel.errorMessage != null && pageModel.entries.isEmpty() -> {
                ErrorContent(
                    message = pageModel.errorMessage ?: "加载失败",
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            pageModel.entries.isEmpty() -> {
                EmptyContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            else -> {
                PagedList(
                    data = PagedData(
                        items = pageModel.entries,
                        isRefreshing = pageModel.isLoading,
                        isLoadingMore = pageModel.isLoadingMore,
                        canLoadMore = pageModel.canLoadMore
                    ),
                    onRefresh = onRefresh,
                    onLoadMore = onLoadMore,
                    listState = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    itemKey = { item -> item.stableKey }
                ) { item ->
                    when (item.type) {
                        HomeFeedEntryType.VIDEO -> {
                            item.video?.let { video ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .clickable { onVideoClick(video) },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column {
                                        image(
                                            video.coverUrl,
                                            video.title,
                                            Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .background(Color.LightGray)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            image(
                                                video.authorIcon,
                                                video.authorName,
                                                Modifier
                                                    .size(40.dp)
                                                    .background(
                                                        Color.Gray,
                                                        shape = MaterialTheme.shapes.small
                                                    )
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = video.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = video.subtitle,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HomeFeedEntryType.HEADER -> {
                            Text(
                                text = item.text.orEmpty(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 24.dp
                                )
                            )
                        }

                        HomeFeedEntryType.FOOTER -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.text.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
