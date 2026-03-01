package com.kotlinmvvm.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.ui.state.PagedData
import com.kotlinmvvm.core.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: EyepetizerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<PagedData<EyepetizerFeedItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<PagedData<EyepetizerFeedItem>>> = _uiState.asStateFlow()

    private var nextPageUrl: String? = null
    private val allItems = mutableListOf<EyepetizerFeedItem>()

    init {
        loadFeed()
    }

    fun loadFeed() {
        _uiState.value = UiState.Loading
        allItems.clear()
        nextPageUrl = null
        
        viewModelScope.launch {
            repository.getHomeFeed()
                .onSuccess { feed ->
                    allItems.addAll(feed.items)
                    nextPageUrl = feed.nextPageUrl
                    _uiState.value = UiState.Success(
                        PagedData(items = allItems.toList(), canLoadMore = !feed.nextPageUrl.isNullOrEmpty())
                    )
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.localizedMessage ?: "Unknown error")
                }
        }
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return
        val currentData = currentState.data
        if (currentData.isLoadingMore || !currentData.canLoadMore) return

        _uiState.value = UiState.Success(currentData.copy(isLoadingMore = true))

        viewModelScope.launch {
            repository.getHomeFeed(nextPageUrl)
                .onSuccess { feed ->
                    allItems.addAll(feed.items)
                    nextPageUrl = feed.nextPageUrl
                    _uiState.value = UiState.Success(
                        PagedData(items = allItems.toList(), isLoadingMore = false, canLoadMore = !feed.nextPageUrl.isNullOrEmpty())
                    )
                }
                .onFailure {
                    _uiState.value = UiState.Success(currentData.copy(isLoadingMore = false))
                }
        }
    }

    fun refresh() = loadFeed()

    companion object {
        fun factory(repository: EyepetizerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
        }
    }
}
