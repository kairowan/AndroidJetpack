package com.kotlinmvvm.core.player

import android.content.Context
import android.net.Uri
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 浩楠
 *
 * @date 2026-2-27
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

private const val VIDEO_CACHE_DIR = "media3_video_cache"
private const val VIDEO_CACHE_MAX_BYTES = 300L * 1024 * 1024
internal const val DEFAULT_PRELOAD_BYTES = 2L * 1024 * 1024

/**
 * 统一管理 Media3 视频缓存与预加载任务。
 */
internal object VideoCacheManager {

    @Volatile
    private var simpleCache: SimpleCache? = null

    private val runningPreloads = ConcurrentHashMap<String, Job>()

    fun buildCacheDataSourceFactory(context: Context): DataSource.Factory {
        val appContext = context.applicationContext
        val upstreamFactory = DefaultDataSource.Factory(appContext)
        return CacheDataSource.Factory()
            .setCache(getCache(appContext))
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun preload(
        scope: CoroutineScope,
        context: Context,
        url: String,
        bytes: Long = DEFAULT_PRELOAD_BYTES
    ) {
        if (url.isBlank()) return
        if (runningPreloads[url]?.isActive == true) return

        val preloadJob = scope.launch(Dispatchers.IO) {
            val dataSource = buildCacheDataSourceFactory(context).createDataSource() as CacheDataSource
            val dataSpec = DataSpec.Builder()
                .setUri(Uri.parse(url))
                .setPosition(0)
                .setLength(bytes.coerceAtLeast(1L))
                .build()
            try {
                CacheWriter(
                    dataSource,
                    dataSpec,
                    null,
                    null
                ).cache()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // 预加载失败不影响主播放链路
            } finally {
                runningPreloads.remove(url)
            }
        }
        runningPreloads[url] = preloadJob
    }

    fun preload(
        scope: CoroutineScope,
        context: Context,
        urls: List<String>,
        bytes: Long = DEFAULT_PRELOAD_BYTES
    ) {
        urls.forEach { preload(scope, context, it, bytes) }
    }

    private fun getCache(context: Context): SimpleCache {
        simpleCache?.let { return it }
        return synchronized(this) {
            simpleCache ?: run {
                val cacheDir = File(context.cacheDir, VIDEO_CACHE_DIR)
                val cache = SimpleCache(
                    cacheDir,
                    LeastRecentlyUsedCacheEvictor(VIDEO_CACHE_MAX_BYTES),
                    StandaloneDatabaseProvider(context)
                )
                simpleCache = cache
                cache
            }
        }
    }
}
