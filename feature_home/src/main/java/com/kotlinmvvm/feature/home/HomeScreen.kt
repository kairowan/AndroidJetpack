package com.kotlinmvvm.feature.home

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.data.repository.EyepetizerRepositoryFactory
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.state.PagedData
import com.kotlinmvvm.core.ui.component.ErrorContent
import com.kotlinmvvm.core.ui.component.LoadingContent
import com.kotlinmvvm.core.ui.component.PagedList
import com.kotlinmvvm.core.ui.base.viewModelFactory
import com.kotlinmvvm.feature.home.shared.HomeFeedCatalog
import com.kotlinmvvm.feature.home.shared.HomeFeedEntryType
import com.kotlinmvvm.feature.home.shared.HomeFeedPagePresenter
import com.kotlinmvvm.feature.home.shared.HomeVideoCardModel

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
    repository: EyepetizerRepository? = null,
    providedViewModel: HomeViewModel? = null,
    onVideoClick: (EyepetizerFeedItem.Video) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val owner = remember(context) { context.findViewModelStoreOwner() }
    val routeRepository = repository ?: remember { EyepetizerRepositoryFactory.create() }
    val viewModel = providedViewModel ?: viewModel(
        viewModelStoreOwner = owner ?: checkNotNull(LocalViewModelStoreOwner.current),
        key = "home_root_view_model",
        factory = viewModelFactory {
            HomeViewModel(routeRepository)
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedSource by viewModel.feedSource.collectAsStateWithLifecycle()
    val pageModel = remember(state, selectedSource) {
        HomeFeedPagePresenter.present(
            state = state,
            selectedSource = selectedSource
        )
    }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Eyepetizer") },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            )
//        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            FeedSourceSelector(
                selectedSourceKey = pageModel.selectedSourceKey,
                onSelected = viewModel::switchSource
            )

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
                        message = pageModel.errorMessage ?: "Unknown error",
                        onRetry = { viewModel.retry() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                else -> {
                    PagedList(
                        data = PagedData(
                            items = pageModel.entries,
                            isLoadingMore = pageModel.isLoadingMore,
                            canLoadMore = pageModel.canLoadMore
                        ),
                        onRefresh = { viewModel.refresh() },
                        onLoadMore = { viewModel.loadMore() },
                        listState = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        itemKey = { item -> item.stableKey }
                    ) { item ->
                        when (item.type) {
                            HomeFeedEntryType.VIDEO -> {
                                val video = item.video
                                if (video != null) {
                                    VideoCard(
                                        video = video,
                                        onClick = {
                                            viewModel.findVideo(video.id)?.let(onVideoClick)
                                        }
                                    )
                                }
                            }

                            HomeFeedEntryType.HEADER -> TextHeaderItem(text = item.text.orEmpty())
                            HomeFeedEntryType.FOOTER -> TextFooterItem(text = item.text.orEmpty())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedSourceSelector(
    selectedSourceKey: String,
    onSelected: (com.kotlinmvvm.core.model.EyepetizerFeedSource) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HomeFeedCatalog.sourceOptions.forEach { source ->
            FilterChip(
                selected = source.key == selectedSourceKey,
                onClick = { onSelected(source.source) },
                label = { Text(source.title) }
            )
        }
    }
}

@Composable
fun VideoCard(video: HomeVideoCardModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
                    Text(video.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

private tailrec fun Context.findViewModelStoreOwner(): ViewModelStoreOwner? {
    return when (this) {
        is ViewModelStoreOwner -> this
        is ContextWrapper -> baseContext.findViewModelStoreOwner()
        else -> null
    }
}
