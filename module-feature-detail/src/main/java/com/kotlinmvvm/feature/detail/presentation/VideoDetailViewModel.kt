package com.kotlinmvvm.feature.detail.presentation

import com.kotlinmvvm.core.data.result.DataFailure
import com.kotlinmvvm.core.data.result.DataSuccess
import com.kotlinmvvm.core.ui.viewmodel.BaseViewModel
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver
import com.kotlinmvvm.domain.feed.repository.FeedVideoRepository
import com.kotlinmvvm.domain.feed.result.FeedLoadError
import com.kotlinmvvm.domain.feed.model.FeedSource
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情状态持有者，只从仓库事实流接收内容并管理恢复任务的 Loading 与失败元数据
 */
class VideoDetailViewModel(
    private val videoRepository: FeedVideoRepository,
    private val videoId: Int,
    private val source: FeedSource,
    taskObserver: ViewModelTaskObserver = ViewModelTaskObserver.None
) : BaseViewModel<VideoDetailUiState>(VideoDetailUiState(), taskObserver) {
    private val observedVideo = videoRepository.observeVideo(videoId, source)

    init {
        observedVideo
            .restoreUiState(videoRepository.findCachedVideo(videoId, source)) { video ->
                VideoDetailUiState(isLoading = video == null, video = video)
            }
            .filterNotNull()
            .onEach { video -> setState(VideoDetailUiState(false, video)) }
            .launchLatestIn(TASK_OBSERVE_VIDEO)
        load()
    }

    fun retry() = load()

    private fun load() {
        taskFlow { videoRepository.getVideo(videoId, source) }
            .onEach { result ->
                updateState { state ->
                    when (result) {
                        is DataSuccess -> state.copy(loadError = null)
                        is DataFailure -> if (state.video == null) {
                            state.copy(loadError = result.error)
                        } else {
                            state
                        }
                    }
                }
            }
            .launchUniqueIn(TASK_LOAD_VIDEO, onLoadingChanged = ::setLoading)
    }

    private fun setLoading(isLoading: Boolean) {
        val cachedVideo = videoRepository.findCachedVideo(videoId, source)
        updateState { state ->
            when {
                isLoading -> state.copy(isLoading = state.video == null, loadError = null)
                state.video != null || state.loadError != null -> state.copy(isLoading = false)
                cachedVideo != null -> VideoDetailUiState(
                    isLoading = false,
                    video = cachedVideo
                )
                else -> state.copy(
                    isLoading = false,
                    loadError = FeedLoadError.INVALID_RESPONSE
                )
            }
        }
    }

    private companion object {
        const val TASK_OBSERVE_VIDEO = "detail.observe_video"
        const val TASK_LOAD_VIDEO = "detail.load_video"
    }
}
