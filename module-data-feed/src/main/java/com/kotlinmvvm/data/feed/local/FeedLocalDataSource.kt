package com.kotlinmvvm.data.feed.local

import com.kotlinmvvm.domain.feed.model.FeedSource

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 信息流本地数据源契约，为仓库提供进程恢复所需的页面事实副本
 */
internal interface FeedLocalDataSource {
    /** 读取指定来源缓存；缓存缺失或损坏返回成功空值，临时 I/O 故障返回失败且保留原文件。 */
    suspend fun read(source: FeedSource): Result<FeedCacheEntry?>

    /** 原子写入指定来源的完整页面记录，写入失败时抛出原始 I/O 异常。 */
    suspend fun write(source: FeedSource, entry: FeedCacheEntry)
}
