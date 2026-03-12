package com.kotlinmvvm.shared.ios

import com.kotlinmvvm.core.data.repository.EyepetizerRepository
import com.kotlinmvvm.core.data.repository.EyepetizerRepositoryFactory
import com.kotlinmvvm.core.data.paging.PagedState
import com.kotlinmvvm.feature.home.shared.HomeFeedCatalog
import com.kotlinmvvm.feature.home.shared.HomeFeedEntryType
import com.kotlinmvvm.feature.home.shared.HomeFeedPagePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: iOS 宿主调用的首页共享桥接层
 */
class IosHomeFeedBridge internal constructor(
    private val repository: EyepetizerRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    constructor() : this(EyepetizerRepositoryFactory.create())

    fun defaultSourceKey(): String = HomeFeedCatalog.defaultSource.key

    fun availableSources(): List<IosFeedOption> = HomeFeedCatalog.sourceOptions.asIosFeedOptions()

    fun initialState(): IosFeedScreenState = initialState(HomeFeedCatalog.defaultSource.key)

    fun initialState(sourceKey: String): IosFeedScreenState {
        return HomeFeedPagePresenter.initialState(sourceKey).toIosScreenState()
    }

    fun loadFeed(onResult: (IosFeedScreenState) -> Unit) {
        loadFeed(HomeFeedCatalog.defaultSource.key, null, onResult)
    }

    fun loadFeed(
        sourceKey: String,
        nextPageUrl: String?,
        onResult: (IosFeedScreenState) -> Unit
    ) {
        scope.launch {
            onResult(loadFeedNow(sourceKey, nextPageUrl))
        }
    }

    suspend fun loadFeedNow(
        sourceKey: String = HomeFeedCatalog.defaultSource.key,
        nextPageUrl: String? = null
    ): IosFeedScreenState {
        val selectedSource = HomeFeedCatalog.sourceFromKey(sourceKey)
        return repository.getFeed(selectedSource, nextPageUrl).fold(
            onSuccess = { feed ->
                HomeFeedPagePresenter.present(
                    state = PagedState(
                        items = feed.items,
                        canLoadMore = feed.nextPageUrl != null
                    ),
                    selectedSource = selectedSource
                )
                    .copy(nextPageUrl = feed.nextPageUrl)
                    .toIosScreenState()
            },
            onFailure = { error ->
                HomeFeedPagePresenter.initialState(sourceKey)
                    .copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Feed load failed"
                    )
                    .toIosScreenState()
            }
        )
    }

    fun loadFeed(
        sourceKey: String,
        onResult: (IosFeedScreenState) -> Unit
    ) {
        loadFeed(sourceKey, null, onResult)
    }

    fun cancel() {
        scope.cancel()
    }
}

/**
 *  描述: iOS 侧可直接消费的 feed 选项
 */
data class IosFeedOption(
    val key: String,
    val title: String
)

/**
 *  描述: iOS 首页视频卡片视图模型
 */
data class IosVideoCard(
    val id: Int,
    val title: String,
    val descriptionText: String,
    val subtitle: String,
    val authorName: String,
    val authorHandle: String,
    val authorIcon: String,
    val category: String,
    val duration: Int,
    val categoryDurationLabel: String,
    val coverUrl: String,
    val playUrl: String
)

data class IosHomeFeedEntry(
    val stableKey: String,
    val kind: String,
    val text: String?,
    val video: IosVideoCard?
)

/**
 *  描述: iOS 首页屏幕状态快照
 */
data class IosFeedScreenState(
    val selectedSourceKey: String,
    val title: String,
    val isLoading: Boolean,
    val errorMessage: String?,
    val nextPageUrl: String?,
    val entries: List<IosHomeFeedEntry>,
    val videos: List<IosVideoCard>
)

private fun List<com.kotlinmvvm.feature.home.shared.HomeFeedSourceOption>.asIosFeedOptions(): List<IosFeedOption> {
    return map { option -> IosFeedOption(key = option.key, title = option.title) }
}

private fun com.kotlinmvvm.feature.home.shared.HomeFeedPageModel.toIosScreenState(): IosFeedScreenState {
    val iosEntries = entries.map { entry ->
        IosHomeFeedEntry(
            stableKey = entry.stableKey,
            kind = entry.type.name,
            text = entry.text,
            video = entry.video?.let { video ->
                IosVideoCard(
                    id = video.id,
                    title = video.title,
                    descriptionText = video.descriptionText,
                    subtitle = video.subtitle,
                    authorName = video.authorName,
                    authorHandle = video.authorHandle,
                    authorIcon = video.authorIcon,
                    category = video.category,
                    duration = video.duration,
                    categoryDurationLabel = video.categoryDurationLabel,
                    coverUrl = video.coverUrl,
                    playUrl = video.playUrl
                )
            }
        )
    }

    return IosFeedScreenState(
        selectedSourceKey = selectedSourceKey,
        title = title,
        isLoading = isLoading,
        errorMessage = errorMessage,
        nextPageUrl = nextPageUrl,
        entries = iosEntries,
        videos = iosEntries.mapNotNull(IosHomeFeedEntry::video)
    )
}
