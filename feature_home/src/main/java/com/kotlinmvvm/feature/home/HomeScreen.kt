package com.kotlinmvvm.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.ui.component.ErrorContent
import com.kotlinmvvm.core.ui.component.LoadingContent
import com.kotlinmvvm.core.ui.component.PagedList
import com.kotlinmvvm.core.ui.base.viewModelFactory
import com.kotlinmvvm.core.ui.state.PagedData

/**
 * @author 浩楠
 *
 * @date 2026-2-28
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            HomeViewModel(EyepetizerRepository())
        }
    ),
    onVideoClick: (EyepetizerFeedItem.Video) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eyepetizer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.items.isEmpty() -> {
                LoadingContent(modifier = modifier.padding(paddingValues))
            }

            state.errorMessage != null && state.items.isEmpty() -> {
                ErrorContent(
                    message = state.errorMessage ?: "Unknown error",
                    onRetry = { viewModel.retry() },
                    modifier = modifier.padding(paddingValues)
                )
            }

            else -> {
                PagedList(
                    data = PagedData(
                        items = state.items,
                        isLoadingMore = state.isLoadingMore,
                        canLoadMore = state.canLoadMore
                    ),
                    onRefresh = { viewModel.refresh() },
                    onLoadMore = { viewModel.loadMore() },
                    modifier = modifier.padding(paddingValues),
                    itemKey = { item ->
                        when (item) {
                            is EyepetizerFeedItem.Video -> "video_${item.id}"
                            is EyepetizerFeedItem.TextHeader -> "header_${item.text.hashCode()}"
                            is EyepetizerFeedItem.TextFooter -> "footer_${item.text.hashCode()}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is EyepetizerFeedItem.Video -> VideoCard(video = item, onClick = { onVideoClick(item) })
                        is EyepetizerFeedItem.TextHeader -> TextHeaderItem(text = item.text)
                        is EyepetizerFeedItem.TextFooter -> TextFooterItem(text = item.text)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCard(video: EyepetizerFeedItem.Video, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = video.coverUrl, contentDescription = video.title, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.LightGray)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = video.authorIcon, contentDescription = video.authorName,
                    modifier = Modifier.size(40.dp).background(Color.Gray, shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(video.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${video.authorName} / #${video.category}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun TextHeaderItem(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp))
}

@Composable
fun TextFooterItem(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}
