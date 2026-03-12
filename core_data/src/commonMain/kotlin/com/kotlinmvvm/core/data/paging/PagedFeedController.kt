package com.kotlinmvvm.core.data.paging

/**
 * @author 浩楠
 *
 * @date 2026-3-9
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: 共享的分页状态控制器
 */
class PagedFeedController<T>(
    private val pager: EyepetizerFeedPager<T>
) {
    fun startRefresh(currentState: PagedState<T>): PagedState<T> {
        return currentState.copy(
            isLoading = true,
            isLoadingMore = false,
            errorMessage = null
        )
    }

    fun startLoadMore(currentState: PagedState<T>): PagedState<T> {
        return currentState.copy(
            isLoadingMore = true,
            errorMessage = null
        )
    }

    fun canLoadMore(currentState: PagedState<T>): Boolean {
        return !currentState.isLoading &&
            !currentState.isLoadingMore &&
            currentState.canLoadMore
    }

    suspend fun refresh(): Result<PagedState<T>> {
        return pager.refresh().map { snapshot ->
            snapshot.toPagedState()
        }
    }

    suspend fun loadMore(currentState: PagedState<T>): Result<PagedState<T>> {
        return pager.loadMore().map { snapshot ->
            currentState.copy(
                items = snapshot.items,
                isLoadingMore = false,
                canLoadMore = snapshot.canLoadMore,
                errorMessage = null
            )
        }
    }

    fun errorState(
        currentState: PagedState<T>,
        error: Throwable,
        loadingMore: Boolean
    ): PagedState<T> {
        val hasItems = pager.currentSnapshot().items.isNotEmpty()
        return if (loadingMore) {
            currentState.copy(
                isLoadingMore = false,
                errorMessage = if (hasItems) currentState.errorMessage else error.readableMessage()
            )
        } else {
            currentState.copy(
                isLoading = false,
                isLoadingMore = false,
                errorMessage = error.readableMessage()
            )
        }
    }

    private fun FeedPageSnapshot<T>.toPagedState(): PagedState<T> {
        return PagedState(
            isLoading = false,
            items = items,
            isLoadingMore = false,
            canLoadMore = canLoadMore,
            errorMessage = null
        )
    }
}

private fun Throwable.readableMessage(): String {
    return message ?: "Unknown error"
}
