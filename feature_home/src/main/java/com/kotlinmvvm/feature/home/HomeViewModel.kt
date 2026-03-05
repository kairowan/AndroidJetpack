package com.kotlinmvvm.feature.home

import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.ui.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class HomeViewModel(
    private val repository: EyepetizerRepository
) : BaseViewModel<HomeState>(HomeState()) {

    private var allItems: PersistentList<EyepetizerFeedItem> = persistentListOf()
    private var nextPageUrl: String? = null
    private val requestMutex = Mutex()

    init {
        loadInitial()
    }

    fun loadInitial() {
        launchRequest { loadFirstPage() }
    }

    fun refresh() = loadInitial()

    fun retry() = loadInitial()

    fun loadMore() {
        launchRequest { loadNextPage() }
    }

    private fun launchRequest(block: suspend () -> Unit) {
        viewModelScope.launch {
            if (!requestMutex.tryLock()) return@launch
            try {
                block()
            } finally {
                requestMutex.unlock()
            }
        }
    }

    private suspend fun loadFirstPage() {
        reduce {
            copy(
                isLoading = true,
                isLoadingMore = false,
                errorMessage = null
            )
        }

        allItems = persistentListOf()
        nextPageUrl = null

        repository.getHomeFeed()
            .onSuccess { feed ->
                allItems = allItems.addAll(feed.items)
                nextPageUrl = feed.nextPageUrl
                reduce {
                    copy(
                        isLoading = false,
                        items = allItems,
                        isLoadingMore = false,
                        canLoadMore = !feed.nextPageUrl.isNullOrEmpty(),
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = error.localizedMessage ?: "Unknown error"
                    )
                }
            }
    }

    private suspend fun loadNextPage() {
        val current = state.value
        if (current.isLoading || current.isLoadingMore || !current.canLoadMore) return

        val url = nextPageUrl ?: return

        reduce {
            copy(
                isLoadingMore = true,
                errorMessage = null
            )
        }

        repository.getHomeFeed(url)
            .onSuccess { feed ->
                allItems = allItems.addAll(feed.items)
                nextPageUrl = feed.nextPageUrl
                reduce {
                    copy(
                        items = allItems,
                        isLoadingMore = false,
                        canLoadMore = !feed.nextPageUrl.isNullOrEmpty(),
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    copy(
                        isLoadingMore = false,
                        errorMessage = if (allItems.isEmpty()) {
                            error.localizedMessage ?: "Unknown error"
                        } else {
                            errorMessage
                        }
                    )
                }
            }
    }
}
