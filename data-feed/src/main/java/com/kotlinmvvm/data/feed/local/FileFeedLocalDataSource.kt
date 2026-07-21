package com.kotlinmvvm.data.feed.local

import android.util.AtomicFile
import com.kotlinmvvm.domain.feed.model.FeedSource
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * @author 浩楠
 * @date 2026/7/21 12:56
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 有界且版本化的信息流文件数据源，以 AtomicFile 保证完整写入并主动清除损坏缓存
 */
internal class FileFeedLocalDataSource(
    private val directory: File,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val maxCacheBytes: Long = DEFAULT_MAX_CACHE_BYTES,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
) : FeedLocalDataSource {

    init {
        require(maxCacheBytes > 0L) { "信息流缓存单文件上限必须大于 0" }
    }

    override suspend fun read(source: FeedSource): Result<FeedCacheEntry?> = withContext(ioDispatcher) {
        val file = cacheFile(source)
        if (!file.baseFile.exists()) return@withContext Result.success(null)
        if (file.baseFile.length() !in 1L..maxCacheBytes) {
            file.delete()
            return@withContext Result.success(null)
        }
        try {
            val entry = json.decodeFromString<FeedCacheEntry>(file.readFully().decodeToString())
            if (entry.isCompatible()) {
                Result.success(entry)
            } else {
                file.delete()
                Result.success(null)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: SerializationException) {
            file.delete()
            Result.success(null)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun write(source: FeedSource, entry: FeedCacheEntry) = withContext(ioDispatcher) {
        check(directory.exists() || directory.mkdirs()) { "无法创建信息流缓存目录: $directory" }
        val encodedEntry = json.encodeToString(entry).encodeToByteArray()
        check(encodedEntry.size.toLong() <= maxCacheBytes) {
            "信息流缓存超过单文件上限: ${encodedEntry.size} > $maxCacheBytes"
        }
        val file = cacheFile(source)
        val output = file.startWrite()
        try {
            output.write(encodedEntry)
            file.finishWrite(output)
        } catch (error: CancellationException) {
            file.failWrite(output)
            throw error
        } catch (error: Exception) {
            file.failWrite(output)
            throw error
        }
    }

    private fun cacheFile(source: FeedSource) = AtomicFile(
        File(directory, "${source.name.lowercase()}.json")
    )

    private companion object {
        const val DEFAULT_MAX_CACHE_BYTES = 8L * 1024L * 1024L
    }
}
