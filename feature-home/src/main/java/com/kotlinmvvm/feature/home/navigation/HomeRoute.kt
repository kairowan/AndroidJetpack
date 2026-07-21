package com.kotlinmvvm.feature.home.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver
import com.kotlinmvvm.feature.home.presentation.HomeViewModel
import com.kotlinmvvm.feature.home.ui.HomeScreen

/**
 * @author 浩楠
 * @date 2026/7/20 16:42
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 首页路由入口，在 Navigation 3 页面生命周期内创建 ViewModel 并连接无状态页面
 */
@Composable
fun HomeRoute(
    pageRepository: FeedPageRepository,
    onVideoClick: (FeedVideo, FeedSource) -> Unit,
    taskObserver: ViewModelTaskObserver = ViewModelTaskObserver.None,
    modifier: Modifier = Modifier
) {
    val viewModel: HomeViewModel = viewModel {
        HomeViewModel(pageRepository, createSavedStateHandle(), taskObserver)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onSourceSelected = viewModel::selectSource,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        onErrorShown = viewModel::clearTransientError,
        onVideoClick = { video -> onVideoClick(video, uiState.selectedSource) },
        modifier = modifier
    )
}
