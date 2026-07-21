package com.kotlinmvvm.data.feed.observer

import com.kotlinmvvm.domain.feed.model.FeedSource

/**
 * @author 浩楠
 * @date 2026/7/21 13:35
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 信息流数据层非致命故障观察接口，由宿主按需接入日志、埋点或崩溃平台
 */
fun interface FeedDataObserver {
    /** 接收不会中断业务结果的缓存故障，观察器实现不得向外抛出异常。 */
    fun onCacheFailure(source: FeedSource, operation: FeedCacheOperation, error: Throwable)
}

val NoOpFeedDataObserver = FeedDataObserver { _, _, _ -> }
