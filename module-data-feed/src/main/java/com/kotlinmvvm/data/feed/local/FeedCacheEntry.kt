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
 * 描述: 信息流磁盘缓存信封，携带页面快照、更新时间与格式版本供读取边界安全校验
 */
@Serializable
internal data class FeedCacheEntry(
    val items: List<FeedCacheItem>,
    val nextPageUrl: String?,
    val updatedAtEpochMillis: Long,
    val schemaVersion: Int
)
