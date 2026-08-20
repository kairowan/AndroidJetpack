package com.kotlinmvvm.data.feed.repository

import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo

/**
 * 合并已展示条目与新分页条目，仅按视频稳定编号去重。
 *
 * 文本等结构条目没有稳定业务编号，即使内容相同也必须保留。
 */
internal fun mergeFeedItems(
    current: List<FeedItem>,
    incoming: List<FeedItem>
): List<FeedItem> {
    val knownVideoIds = current.filterIsInstance<FeedVideo>()
        .mapTo(mutableSetOf(), FeedVideo::id)
    return buildList(current.size + incoming.size) {
        addAll(current)
        incoming.forEach { item ->
            if (item !is FeedVideo || knownVideoIds.add(item.id)) add(item)
        }
    }
}
