package com.kotlinmvvm.feature.home

import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.ui.base.BasePagedViewModel
import com.kotlinmvvm.core.ui.base.PagedResult

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

class HomeViewModel(
    private val repository: EyepetizerRepository
) : BasePagedViewModel<EyepetizerFeedItem>() {

    init {
        loadFeed()
    }

    fun loadFeed() {
        loadFirst {
            repository.getHomeFeed().map { feed ->
                PagedResult(
                    items = feed.items,
                    nextPageUrl = feed.nextPageUrl
                )
            }
        }
    }

    fun loadMore() {
        loadMore { url ->
            repository.getHomeFeed(url).map { feed ->
                PagedResult(
                    items = feed.items,
                    nextPageUrl = feed.nextPageUrl
                )
            }
        }
    }

    fun refresh() = loadFeed()
}
