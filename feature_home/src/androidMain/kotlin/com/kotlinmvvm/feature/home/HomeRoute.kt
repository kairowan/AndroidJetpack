package com.kotlinmvvm.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.ui.base.viewModelFactory
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.feature.home.shared.HomeFeedPagePresenter

/**
 * @author 浩楠
 * @date 2026/7/24 11:50
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Android 首页路由，只负责 Navigation 3 入口作用域、生命周期收集和 Coil 图片适配
 */
@Composable
fun HomeRoute(
    repository: FeedPageRepository,
    onVideoClick: (EyepetizerFeedItem.Video) -> Unit,
    modifier: Modifier = Modifier,
    providedViewModel: HomeViewModel? = null
) {
    val homeViewModel = providedViewModel ?: viewModel(
        factory = viewModelFactory { HomeViewModel(repository) }
    )
    val state by homeViewModel.state.collectAsStateWithLifecycle()
    val selectedSource by homeViewModel.feedSource.collectAsStateWithLifecycle()
    val pageModel = remember(state, selectedSource) {
        HomeFeedPagePresenter.present(
            state = state,
            selectedSource = selectedSource
        )
    }

    HomeScreen(
        pageModel = pageModel,
        onSourceSelected = homeViewModel::switchSource,
        onRetry = homeViewModel::retry,
        onRefresh = homeViewModel::refresh,
        onLoadMore = homeViewModel::loadMore,
        onVideoClick = { video ->
            homeViewModel.findVideo(video.id)?.let(onVideoClick)
        },
        image = { url, contentDescription, imageModifier ->
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = imageModifier
            )
        },
        modifier = modifier
    )
}
