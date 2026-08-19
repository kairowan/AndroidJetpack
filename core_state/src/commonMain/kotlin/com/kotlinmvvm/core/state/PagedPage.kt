package com.kotlinmvvm.core.state

/**
 * @author 浩楠
 * @date 2026/7/24 13:44
 * 描述: 一次分页请求完成后的完整页面快照
 */
data class PagedPage<T>(
    val items: List<T>,
    val canLoadMore: Boolean
)
