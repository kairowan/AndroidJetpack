package com.kotlinmvvm.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlinmvvm.core.ui.state.PagedData
import com.kotlinmvvm.core.ui.state.UiState

/**
 * 通用加载状态组件
 */
@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * 通用错误状态组件
 */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "加载失败",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

/**
 * 通用空状态组件
 */
@Composable
fun EmptyContent(
    message: String = "暂无数据",
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 加载更多指示器
 */
@Composable
fun LoadMoreIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
    }
}

/**
 * 没有更多数据提示
 */
@Composable
fun NoMoreContent(
    text: String = "— 没有更多了 —",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 通用 UiState 容器
 */
@Composable
fun <T> UiStateContainer(
    uiState: UiState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = { LoadingContent() },
    errorContent: @Composable (String) -> Unit = { ErrorContent(it, onRetry) },
    content: @Composable (T) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is UiState.Loading -> loadingContent()
            is UiState.Error -> errorContent(uiState.message)
            is UiState.Success -> content(uiState.data)
        }
    }
}

/**
 * 通用分页列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PagedList(
    data: PagedData<T>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= data.items.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && data.canLoadMore && !data.isLoadingMore) {
            onLoadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
            isRefreshing = false
        },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                count = data.items.size,
                key = itemKey?.let { keyFn -> { index -> keyFn(data.items[index]) } }
            ) { index ->
                itemContent(data.items[index])
            }

            if (data.isLoadingMore) {
                item { LoadMoreIndicator() }
            }

            if (!data.canLoadMore && data.items.isNotEmpty()) {
                item { NoMoreContent() }
            }
        }
    }
}
