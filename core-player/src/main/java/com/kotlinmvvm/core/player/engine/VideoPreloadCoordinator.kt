package com.kotlinmvvm.core.player.engine

import android.content.Context
import androidx.media3.datasource.DataSource
import com.kotlinmvvm.core.player.cache.VideoCacheStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 页面级视频预加载协调器，保留当前目标、取消过期下载并复用进程级缓存去重
 */
internal class VideoPreloadCoordinator(
    private val scope: CoroutineScope,
    private val context: Context,
    private val upstreamFactory: DataSource.Factory
) {
    private val jobs = mutableMapOf<String, Job>()

    fun preload(url: String, bytes: Long) {
        if (jobs[url]?.isActive == true) return
        VideoCacheStore.preload(scope, context, upstreamFactory, url, bytes)
            ?.let { jobs[url] = it }
    }

    fun replace(urls: List<String>, bytes: Long) {
        val requestedUrls = urls.filter(String::isNotBlank).toSet()
        jobs.entries.removeAll { (url, job) ->
            (url !in requestedUrls).also { stale -> if (stale) job.cancel() }
        }
        requestedUrls.forEach { url ->
            if (jobs[url]?.isActive != true) preload(url, bytes)
        }
    }

    fun cancel() {
        jobs.values.forEach(Job::cancel)
        jobs.clear()
    }
}
