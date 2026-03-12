package com.kotlinmvvm.core.player.cache

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
import com.kotlinmvvm.core.player.defaults.PlayerDefaults
import com.kotlinmvvm.core.player.defaults.VideoCacheDefaults
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 视频缓存与预加载仓库（实例级，不使用全局单例）。
 */
internal class VideoCacheStore(
    private val appContext: Context,
    private val maxBytes: Long = VideoCacheDefaults.MAX_BYTES
) {
    private var simpleCache: SimpleCache? = null
    private val cacheLock = Any()
    private val runningPreloads = ConcurrentHashMap<String, Job>()

    fun buildDataSourceFactory(): DataSource.Factory {
        val upstreamFactory = DefaultDataSource.Factory(appContext)
        return CacheDataSource.Factory()
            .setCache(getCache())
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun preload(
        scope: CoroutineScope,
        url: String,
        bytes: Long = PlayerDefaults.PRELOAD_BYTES
    ) {
        if (url.isBlank()) return
        if (runningPreloads[url]?.isActive == true) return

        val preloadJob = scope.launch(Dispatchers.IO) {
            val dataSource = buildDataSourceFactory().createDataSource() as CacheDataSource
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
        urls: List<String>,
        bytes: Long = PlayerDefaults.PRELOAD_BYTES
    ) {
        urls.forEach { preload(scope, it, bytes) }
    }

    fun release() {
        synchronized(cacheLock) {
            simpleCache?.release()
            simpleCache = null
        }
    }

    private fun getCache(): SimpleCache {
        simpleCache?.let { return it }
        return synchronized(cacheLock) {
            simpleCache ?: run {
                val cacheDir = File(appContext.cacheDir, VideoCacheDefaults.DIRECTORY)
                SimpleCache(
                    cacheDir,
                    LeastRecentlyUsedCacheEvictor(maxBytes),
                    StandaloneDatabaseProvider(appContext)
                ).also { cache -> simpleCache = cache }
            }
        }
    }
}
