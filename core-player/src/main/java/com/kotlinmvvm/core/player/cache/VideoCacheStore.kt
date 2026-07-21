package com.kotlinmvvm.core.player.cache

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.kotlinmvvm.core.player.defaults.PlayerDefaults
import com.kotlinmvvm.core.player.defaults.VIDEO_CACHE_DIR
import com.kotlinmvvm.core.player.defaults.VIDEO_CACHE_MAX_BYTES
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 进程级 Media3 视频缓存所有者，复用 SimpleCache 并去重并发预加载任务
 */
@OptIn(UnstableApi::class)
internal object VideoCacheStore {
    @Volatile
    private var simpleCache: SimpleCache? = null
    private val cacheLock = Any()
    private val runningPreloads = ConcurrentHashMap<String, Job>()
    private val preloadSlots = Semaphore(PlayerDefaults.MAX_CONCURRENT_PRELOADS)

    fun buildDataSourceFactory(
        context: Context,
        upstreamFactory: DataSource.Factory
    ): DataSource.Factory {
        val appContext = context.applicationContext
        val defaultDataSourceFactory = DefaultDataSource.Factory(appContext, upstreamFactory)
        return CacheDataSource.Factory()
            .setCache(getCache(appContext))
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun preload(
        scope: CoroutineScope,
        context: Context,
        upstreamFactory: DataSource.Factory,
        url: String,
        bytes: Long = PlayerDefaults.PRELOAD_BYTES
    ): Job? {
        if (url.isBlank()) return null
        if (runningPreloads[url]?.isActive == true) return null

        val preloadJob = scope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
            try {
                preloadSlots.withPermit {
                    val dataSource = buildDataSourceFactory(
                        context,
                        upstreamFactory
                    ).createDataSource() as CacheDataSource
                    val dataSpec = DataSpec.Builder()
                        .setUri(url.toUri())
                        .setPosition(0)
                        .setLength(bytes.coerceAtLeast(1L))
                        .build()
                    CacheWriter(
                        dataSource,
                        dataSpec,
                        null,
                        null
                    ).cache()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // 预加载是机会型优化，失败后正式播放仍会回退到正常上游请求。
            } finally {
                runningPreloads.remove(url, coroutineContext.job)
            }
        }
        return if (runningPreloads.putIfAbsent(url, preloadJob) == null) {
            preloadJob.start()
            preloadJob
        } else {
            preloadJob.cancel()
            null
        }
    }

    private fun getCache(appContext: Context): SimpleCache {
        simpleCache?.let { return it }
        return synchronized(cacheLock) {
            simpleCache ?: run {
                val cacheDir = File(appContext.cacheDir, VIDEO_CACHE_DIR)
                SimpleCache(
                    cacheDir,
                    LeastRecentlyUsedCacheEvictor(VIDEO_CACHE_MAX_BYTES),
                    StandaloneDatabaseProvider(appContext)
                ).also { cache -> simpleCache = cache }
            }
        }
    }
}
