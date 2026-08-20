package com.kotlinmvvm.data.feed.local

import kotlinx.serialization.Serializable

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 信息流磁盘条目快照，隔离序列化格式与领域模型并支持缓存格式独立演进
 */
@Serializable
internal data class FeedCacheItem(
    val type: String,
    val id: Int? = null,
    val text: String? = null,
    val title: String? = null,
    val description: String? = null,
    val coverUrl: String? = null,
    val playUrl: String? = null,
    val category: String? = null,
    val authorName: String? = null,
    val authorIconUrl: String? = null,
    val durationSeconds: Int? = null
)
