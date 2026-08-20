package com.kotlinmvvm.feature.home.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kotlinmvvm.core.designsystem.theme.AppTheme
import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedTextFooter
import com.kotlinmvvm.domain.feed.model.FeedTextHeader
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.core.ui.feedback.EmptyContent
import com.kotlinmvvm.core.ui.feedback.ErrorContent
import com.kotlinmvvm.core.ui.feedback.LoadingContent
import com.kotlinmvvm.feature.home.R
import com.kotlinmvvm.feature.home.presentation.HomeUiState
import com.kotlinmvvm.feature.home.ui.component.FeedSourceSelector
import com.kotlinmvvm.feature.home.ui.component.FeedFooterText
import com.kotlinmvvm.feature.home.ui.component.FeedHeaderText
import com.kotlinmvvm.feature.home.ui.component.HomeFeedList
import com.kotlinmvvm.feature.home.ui.component.HomeVideoCard

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 首页无状态页面，渲染信息流状态并向路由层回传明确的用户操作
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSourceSelected: (FeedSource) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onErrorShown: () -> Unit,
    onVideoClick: (FeedVideo) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = uiState.loadError?.let { stringResource(it.messageResource) }

    LaunchedEffect(errorMessage, uiState.items.isNotEmpty()) {
        if (errorMessage != null && uiState.items.isNotEmpty()) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            FeedSourceSelector(uiState.selectedSource, onSourceSelected)
            when {
                uiState.isInitialLoading -> LoadingContent(Modifier.fillMaxWidth().weight(1f))
                uiState.loadError != null && uiState.items.isEmpty() -> ErrorContent(
                    message = errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                uiState.items.isEmpty() -> EmptyContent(Modifier.fillMaxWidth().weight(1f))
                else -> HomeFeedList(
                    items = uiState.items,
                    isRefreshing = uiState.isRefreshing,
                    isLoadingMore = uiState.isLoadingMore,
                    canLoadMore = uiState.canLoadMore,
                    onRefresh = onRefresh,
                    onLoadMore = onLoadMore,
                    listState = listState,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) { item ->
                    when (item) {
                        is FeedVideo -> HomeVideoCard(
                            video = item,
                            onClick = { onVideoClick(item) }
                        )
                        is FeedTextHeader -> FeedHeaderText(item.text)
                        is FeedTextFooter -> FeedFooterText(item.text)
                    }
                }
            }
        }
    }
}

@get:StringRes
private val FeedLoadError.messageResource: Int
    get() = when (this) {
        FeedLoadError.NO_CONNECTION -> R.string.home_error_no_connection
        FeedLoadError.TIMEOUT -> R.string.home_error_timeout
        FeedLoadError.UNAUTHORIZED -> R.string.home_error_unauthorized
        FeedLoadError.SERVER -> R.string.home_error_server
        FeedLoadError.INVALID_RESPONSE -> R.string.home_error_invalid_response
        FeedLoadError.NOT_FOUND -> R.string.home_error_not_found
        FeedLoadError.UNKNOWN -> R.string.home_error_unknown
    }

@Preview(showBackground = true)
@Composable
private fun HomeScreenContentPreview() {
    AppTheme {
        HomeScreen(
            uiState = HomeUiState(
                items = listOf(
                    FeedTextHeader(stringResource(R.string.home_preview_header)),
                    FeedVideo(
                        id = 1,
                        title = stringResource(R.string.home_preview_title),
                        description = stringResource(R.string.home_preview_description),
                        coverUrl = "",
                        playUrl = "https://media.example.com/compose.mp4",
                        category = stringResource(R.string.home_preview_category),
                        authorName = stringResource(R.string.home_preview_author),
                        authorIconUrl = "",
                        durationSeconds = 180
                    )
                )
            ),
            onSourceSelected = {},
            onRefresh = {},
            onRetry = {},
            onLoadMore = {},
            onErrorShown = {},
            onVideoClick = {}
        )
    }
}
