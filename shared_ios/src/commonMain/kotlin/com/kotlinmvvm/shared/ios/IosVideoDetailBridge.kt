package com.kotlinmvvm.shared.ios

import com.kotlinmvvm.core.data.state.VideoDetailState
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.feature.media.shared.VideoDetailPagePresenter

/**
 * @author 浩楠
 * @date 2026-3-12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: iOS 宿主调用的详情页共享桥接层
 */
class IosVideoDetailBridge {
    fun screenState(video: IosVideoCard): IosVideoDetailScreenState {
        return VideoDetailPagePresenter.present(
            video = video.toDomainVideo(),
            state = VideoDetailState()
        ).toIosScreenState()
    }

    private fun IosVideoCard.toDomainVideo(): EyepetizerFeedItem.Video {
        return EyepetizerFeedItem.Video(
            id = id,
            title = title,
            description = descriptionText,
            coverUrl = coverUrl,
            playUrl = playUrl,
            category = category,
            authorName = authorName,
            authorIcon = authorIcon,
            duration = duration
        )
    }
}

/**
 *  描述: iOS 详情页状态
 */
data class IosVideoDetailScreenState(
    val title: String,
    val playUrl: String,
    val metadataLabel: String,
    val authorName: String,
    val authorIcon: String,
    val descriptionText: String
)

private fun com.kotlinmvvm.feature.media.shared.VideoDetailPageModel.toIosScreenState(): IosVideoDetailScreenState {
    return IosVideoDetailScreenState(
        title = title,
        playUrl = playUrl,
        metadataLabel = metadataLabel,
        authorName = authorName,
        authorIcon = authorIcon,
        descriptionText = descriptionText
    )
}
