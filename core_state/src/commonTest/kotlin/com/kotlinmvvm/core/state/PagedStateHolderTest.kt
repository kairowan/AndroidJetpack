package com.kotlinmvvm.core.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PagedStateHolderTest {
    @Test
    fun failedLoadMoreKeepsItemsAndClearsBusyFlags() = runBlocking {
        val holder = PagedStateHolder(
            scope = CoroutineScope(coroutineContext),
            refreshPage = {
                Result.success(PagedPage(items = listOf("first"), canLoadMore = true))
            },
            loadNextPage = {
                Result.failure(IllegalStateException("internal detail"))
            },
            userMessage = { "加载失败" }
        )

        holder.refreshNow()
        holder.loadMoreNow()

        assertEquals(listOf("first"), holder.state.value.items)
        assertEquals("加载失败", holder.state.value.errorMessage)
        assertFalse(holder.state.value.isLoading)
        assertFalse(holder.state.value.isLoadingMore)
        assertTrue(holder.state.value.canLoadMore)
    }
}
