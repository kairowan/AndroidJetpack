package com.kotlinmvvm.domain.feed.repository

import com.kotlinmvvm.core.data.result.DataResult
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.domain.feed.result.FeedLoadError
import kotlinx.coroutines.flow.Flow

/**
 * @author 浩楠
 * @date 2026/7/21 17:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Feed 视频仓库角色，仅向详情页面暴露视频观察与按稳定编号恢复能力
 */
interface FeedVideoRepository {
    /** 观察指定来源和 ID 的视频事实；订阅结束后仓库不得永久保留该 ID 的独立状态容器。 */
    fun observeVideo(videoId: Int, source: FeedSource): Flow<FeedVideo?>

    /** 同步读取内存中的视频快照；没有命中时返回 null，且不会触发磁盘或网络请求。 */
    fun findCachedVideo(videoId: Int, source: FeedSource): FeedVideo?

    /** 按 ID 和来源恢复视频；缓存与刷新均未命中时返回稳定的未找到结果。 */
    suspend fun getVideo(
        videoId: Int,
        source: FeedSource
    ): DataResult<FeedVideo, FeedLoadError>
}
