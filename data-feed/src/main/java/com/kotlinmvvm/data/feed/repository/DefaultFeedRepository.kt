package com.kotlinmvvm.data.feed.repository

import com.kotlinmvvm.core.network.result.NetworkResult
import com.kotlinmvvm.core.network.result.NetworkError
import com.kotlinmvvm.core.network.result.NetworkSuccess
import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy
import com.kotlinmvvm.data.feed.local.FeedLocalDataSource
import com.kotlinmvvm.data.feed.local.isFeedCacheFresh
import com.kotlinmvvm.data.feed.local.toCacheEntry
import com.kotlinmvvm.data.feed.local.toDomainPage
import com.kotlinmvvm.data.feed.mapper.toDomain
import com.kotlinmvvm.data.feed.mapper.toFeedError
import com.kotlinmvvm.data.feed.observer.FeedCacheOperation
import com.kotlinmvvm.data.feed.observer.FeedDataObserver
import com.kotlinmvvm.data.feed.observer.NoOpFeedDataObserver
import com.kotlinmvvm.data.feed.remote.FeedRemoteDataSource
import com.kotlinmvvm.data.feed.remote.model.FeedRemotePage
import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.domain.feed.result.FeedLoadResult
import com.kotlinmvvm.domain.feed.result.FeedLoadFailure
import com.kotlinmvvm.domain.feed.result.FeedLoadSuccess
import com.kotlinmvvm.domain.feed.result.FeedVideoResult
import com.kotlinmvvm.domain.feed.result.FeedVideoFailure
import com.kotlinmvvm.domain.feed.result.FeedVideoSuccess
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.domain.feed.model.FeedPage
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.domain.feed.repository.FeedVideoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 信息流仓库默认实现，协调版本化缓存、安全地址映射、单次远程恢复和分页事实合并
 */
internal class DefaultFeedRepository(
    private val remoteDataSource: FeedRemoteDataSource,
    private val localDataSource: FeedLocalDataSource,
    private val remoteResourceUrlPolicy: RemoteResourceUrlPolicy,
    private val dataObserver: FeedDataObserver = NoOpFeedDataObserver,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val cacheFreshnessMillis: Long = DEFAULT_CACHE_FRESHNESS_MILLIS
) : FeedPageRepository, FeedVideoRepository {

    init {
        require(cacheFreshnessMillis >= 0L) { "缓存新鲜期不能小于 0" }
    }

    private val pages = FeedSource.entries.associateWith { MutableStateFlow<FeedPage?>(null) }
    private val nextPageUrls = ConcurrentHashMap<FeedSource, String>()
    private val updatedAt = ConcurrentHashMap<FeedSource, Long>()
    private val cacheRestored = ConcurrentHashMap.newKeySet<FeedSource>()
    private val sourceMutexes = FeedSource.entries.associateWith { Mutex() }

    override fun observePage(key: FeedSource): StateFlow<FeedPage?> =
        pages.getValue(key).asStateFlow()

    override fun observeVideo(videoId: Int, source: FeedSource): Flow<FeedVideo?> {
        require(videoId > 0) { "视频 ID 必须大于 0" }
        return pages.getValue(source)
            .map { page -> page.findVideo(videoId) }
            .distinctUntilChanged()
    }

    override fun findCachedVideo(videoId: Int, source: FeedSource): FeedVideo? {
        require(videoId > 0) { "视频 ID 必须大于 0" }
        return pages.getValue(source).value.findVideo(videoId)
    }

    override suspend fun load(params: FeedSource): FeedLoadResult = withSourceLock(params) {
        val key = params
        restoreCacheIfNeeded(key)
        val page = pages.getValue(key).value
        val cachedAt = updatedAt[key]
        if (
            page != null && cachedAt != null && isFeedCacheFresh(
                updatedAtEpochMillis = cachedAt,
                currentTimeMillis = currentTimeMillis(),
                freshnessMillis = cacheFreshnessMillis
            )
        ) {
            FeedLoadSuccess(page)
        } else {
            requestFirstPage(key)
        }
    }

    override suspend fun refreshPage(key: FeedSource): FeedLoadResult = withSourceLock(key) {
        restoreCacheIfNeeded(key)
        requestFirstPage(key)
    }

    override suspend fun loadNextPage(key: FeedSource): FeedLoadResult = withSourceLock(key) {
        restoreCacheIfNeeded(key)
        val currentPage = pages.getValue(key).value ?: return@withSourceLock requestFirstPage(key)
        val nextPageUrl = nextPageUrls[key] ?: return@withSourceLock FeedLoadSuccess(currentPage)
        when (val result = loadRemote(key, nextPageUrl)) {
            is NetworkError -> FeedLoadFailure(result.error.toFeedError())
            is NetworkSuccess -> storePage(
                source = key,
                page = FeedPage(
                    items = mergeItems(currentPage.items, result.value.page.items),
                    canLoadMore = result.value.nextPageUrl != null
                ),
                nextPageUrl = result.value.nextPageUrl
            )
        }
    }

    override suspend fun getVideo(videoId: Int, source: FeedSource): FeedVideoResult =
        withSourceLock(source) {
            require(videoId > 0) { "视频 ID 必须大于 0" }
            restoreCacheIfNeeded(source)
            findVideo(videoId, source)?.let { return@withSourceLock FeedVideoSuccess(it) }

            val refreshed = requestFirstPage(source)
            findVideo(videoId, source)?.let { return@withSourceLock FeedVideoSuccess(it) }
            when (refreshed) {
                is FeedLoadFailure -> FeedVideoFailure(refreshed.error)
                is FeedLoadSuccess -> FeedVideoFailure(FeedLoadError.NOT_FOUND)
            }
        }

    private suspend fun requestFirstPage(source: FeedSource): FeedLoadResult =
        when (val result = loadRemote(source, nextPageUrl = null)) {
            is NetworkError -> FeedLoadFailure(result.error.toFeedError())
            is NetworkSuccess -> storePage(
                source = source,
                page = result.value.page,
                nextPageUrl = result.value.nextPageUrl
            )
        }

    private suspend fun loadRemote(
        source: FeedSource,
        nextPageUrl: String?
    ): NetworkResult<FeedRemotePage> {
        val networkResult = if (nextPageUrl == null) {
            when (source) {
                FeedSource.HOME_SELECTED -> remoteDataSource.fetchSelected()
                FeedSource.DISCOVERY -> remoteDataSource.fetchDiscovery()
                FeedSource.FOLLOW -> remoteDataSource.fetchFollow()
                FeedSource.DISCOVERY_HOT -> remoteDataSource.fetchHot()
                FeedSource.DISCOVERY_CATEGORY -> remoteDataSource.fetchCategories()
                FeedSource.AUTHORS -> remoteDataSource.fetchAuthors()
            }
        } else {
            remoteDataSource.fetchNextPage(nextPageUrl)
        }

        return when (networkResult) {
            is NetworkSuccess -> NetworkSuccess(
                networkResult.value.toDomain(
                    remoteResourceUrlPolicy = remoteResourceUrlPolicy,
                    requestedNextPageUrl = nextPageUrl
                )
            )

            is NetworkError -> networkResult
        }
    }

    private suspend fun storePage(
        source: FeedSource,
        page: FeedPage,
        nextPageUrl: String?
    ): FeedLoadSuccess {
        val timestamp = currentTimeMillis()
        val storedPage = page.copy(canLoadMore = nextPageUrl != null)
        pages.getValue(source).value = storedPage
        updateNextPageUrl(source, nextPageUrl)
        updatedAt[source] = timestamp
        try {
            localDataSource.write(source, storedPage.toCacheEntry(nextPageUrl, timestamp))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            notifyCacheFailure(source, FeedCacheOperation.WRITE, error)
        }
        return FeedLoadSuccess(storedPage)
    }

    private suspend fun restoreCacheIfNeeded(source: FeedSource) {
        if (source in cacheRestored) return
        val entry = localDataSource.read(source).getOrElse { error ->
            notifyCacheFailure(source, FeedCacheOperation.READ, error)
            return
        }
        cacheRestored.add(source)
        if (entry == null) return
        pages.getValue(source).value = entry.toDomainPage(remoteResourceUrlPolicy)
        updateNextPageUrl(source, entry.nextPageUrl)
        updatedAt[source] = entry.updatedAtEpochMillis
    }

    private fun notifyCacheFailure(
        source: FeedSource,
        operation: FeedCacheOperation,
        error: Throwable
    ) {
        runCatching { dataObserver.onCacheFailure(source, operation, error) }
        // ponytail: 监控回调不得反向破坏业务；需要可靠投递时由宿主观察器写入持久队列。
    }

    private fun findVideo(videoId: Int, source: FeedSource): FeedVideo? =
        pages.getValue(source).value.findVideo(videoId)

    private fun FeedPage?.findVideo(videoId: Int): FeedVideo? =
        this?.items.orEmpty().asSequence()
            .filterIsInstance<FeedVideo>()
            .firstOrNull { it.id == videoId }

    private fun updateNextPageUrl(source: FeedSource, nextPageUrl: String?) {
        if (nextPageUrl == null) nextPageUrls.remove(source) else nextPageUrls[source] = nextPageUrl
    }

    private suspend fun <T> withSourceLock(source: FeedSource, block: suspend () -> T): T =
        sourceMutexes.getValue(source).withLock { block() }

    private fun mergeItems(current: List<FeedItem>, incoming: List<FeedItem>): List<FeedItem> {
        val knownVideoIds = current.filterIsInstance<FeedVideo>()
            .mapTo(mutableSetOf(), FeedVideo::id)
        return buildList(current.size + incoming.size) {
            addAll(current)
            incoming.forEach { item ->
                if (item !is FeedVideo || knownVideoIds.add(item.id)) add(item)
            }
        }
    }

    private companion object {
        const val DEFAULT_CACHE_FRESHNESS_MILLIS = 5 * 60 * 1_000L
    }
}
