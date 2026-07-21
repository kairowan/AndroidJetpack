package com.kotlinmvvm.domain.feed.repository

import com.kotlinmvvm.core.data.repository.CommonRepository
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.domain.feed.result.FeedLoadResult
import kotlinx.coroutines.flow.StateFlow

/**
 * @author 浩楠
 * @date 2026/7/20 17:51
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Feed 列表仓库角色，在公共加载契约之上声明 Feed 观察、刷新与分页能力
 */
interface FeedPageRepository : CommonRepository<FeedSource, FeedLoadResult> {
    /**
     * 观察指定 [key] 对应的 Feed 页面数据。
     *
     * 尚无缓存或远程结果时，当前值为 `null`。
     */
    fun observePage(key: FeedSource): StateFlow<FeedPage?>

    /**
     * 忽略缓存时效并刷新指定 [key] 的 Feed 页面。
     *
     * 返回稳定业务结果，不得向上层泄漏传输层异常。
     */
    suspend fun refreshPage(key: FeedSource): FeedLoadResult

    /**
     * 使用仓库持有的分页位置加载指定 [key] 的下一页。
     *
     * 没有下一页时，返回当前稳定结果。
     */
    suspend fun loadNextPage(key: FeedSource): FeedLoadResult
}
