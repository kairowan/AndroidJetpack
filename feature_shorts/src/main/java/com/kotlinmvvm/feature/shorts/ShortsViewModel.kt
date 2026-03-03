package com.kotlinmvvm.feature.shorts

import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.ui.base.BasePagedViewModel
import com.kotlinmvvm.core.ui.base.PagedResult

/**
 * @author 浩楠
 *
 * @date 2026-3-1
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

class ShortsViewModel(
    private val repository: EyepetizerRepository
) : BasePagedViewModel<EyepetizerFeedItem.Video>() {

    init {
        loadShorts()
    }

    fun loadShorts() {
        loadFirst {
            repository.getHomeFeed().map { feed ->
                PagedResult(
                    items = feed.items.filterIsInstance<EyepetizerFeedItem.Video>(),
                    nextPageUrl = feed.nextPageUrl
                )
            }
        }
    }

    fun loadMore() {
        loadMore { url ->
            repository.getHomeFeed(url).map { feed ->
                PagedResult(
                    items = feed.items.filterIsInstance<EyepetizerFeedItem.Video>(),
                    nextPageUrl = feed.nextPageUrl
                )
            }
        }
    }

    fun refresh() {
        loadShorts()
    }
}
