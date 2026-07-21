package com.kotlinmvvm.feature.detail.presentation

import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情不可变页面状态，表达按 ID 恢复期间的加载、内容和稳定失败
 */
data class VideoDetailUiState(
    val isLoading: Boolean = true,
    val video: FeedVideo? = null,
    val loadError: FeedLoadError? = null
)
