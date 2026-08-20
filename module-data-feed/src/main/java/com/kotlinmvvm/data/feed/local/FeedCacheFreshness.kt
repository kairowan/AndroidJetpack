package com.kotlinmvvm.data.feed.local

/**
 * 判断缓存是否仍处于新鲜窗口；未来时间和负时间戳均视为无效，避免时钟回拨误用缓存。
 */
internal fun isFeedCacheFresh(
    updatedAtEpochMillis: Long,
    currentTimeMillis: Long,
    freshnessMillis: Long
): Boolean {
    require(freshnessMillis >= 0L)
    if (updatedAtEpochMillis < 0L || currentTimeMillis < updatedAtEpochMillis) return false
    return currentTimeMillis - updatedAtEpochMillis <= freshnessMillis
}
