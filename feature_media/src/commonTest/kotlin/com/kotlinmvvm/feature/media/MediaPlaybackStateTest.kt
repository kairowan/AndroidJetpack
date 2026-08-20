package com.kotlinmvvm.feature.media

import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.feature.detail.VideoDetailPagePresenter
import com.kotlinmvvm.feature.detail.VideoDetailState
import com.kotlinmvvm.feature.detail.VideoDetailStateHolder
import com.kotlinmvvm.feature.shorts.ShortsPlaybackState
import com.kotlinmvvm.feature.shorts.ShortsPlaybackStateHolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaPlaybackStateTest {
    @Test
    fun detailPresenterCreatesPortableFullscreenPageModel() {
        val video = EyepetizerFeedItem.Video(
            id = 7,
            title = "KMP",
            description = "Shared detail",
            coverUrl = "https://example.com/cover.jpg",
            playUrl = "https://example.com/video.mp4",
            category = "技术",
            authorName = "Compose",
            authorIcon = "https://example.com/avatar.jpg",
            duration = 90
        )

        val pageModel = VideoDetailPagePresenter.present(
            video = video,
            state = VideoDetailState(fullscreenMode = FullscreenMode.LANDSCAPE)
        )

        assertEquals("KMP", pageModel.title)
        assertEquals("#技术 · 1:30", pageModel.metadataLabel)
        assertTrue(pageModel.isFullscreen)
        assertTrue(pageModel.isLandscapeFullscreen)
        assertEquals("切换竖屏", pageModel.controlsCopy.toggleOrientationLabel)
    }

    @Test
    fun shortsPageAndFullscreenTransitionsStayInBounds() {
        val holder = ShortsPlaybackStateHolder(
            ShortsPlaybackState(currentPage = 8)
        )

        assertEquals(2, holder.state.value.normalizedCurrentPage(totalCount = 3))

        holder.enterLandscapeFullscreen()
        holder.toggleFullscreenOrientation()

        assertEquals(FullscreenMode.PORTRAIT, holder.state.value.fullscreenMode)
        holder.exitFullscreen()
        assertFalse(holder.state.value.isFullscreen)
    }

    @Test
    fun detailBackClosesFullscreenBeforePoppingAndKeepsValidSnapshot() {
        val holder = VideoDetailStateHolder()

        holder.enterLandscapeFullscreen()
        assertFalse(holder.onBackPressed())
        assertEquals(FullscreenMode.NONE, holder.state.value.fullscreenMode)
        assertTrue(holder.onBackPressed())

        holder.syncPlaybackSnapshot(positionMs = 8_000L, isPlaying = true, speed = 3f)
        holder.syncPlaybackSnapshot(positionMs = 0L, isPlaying = false, speed = 0.1f)

        assertEquals(8_000L, holder.state.value.playbackSnapshot.positionMs)
        assertFalse(holder.state.value.playbackSnapshot.isPlaying)
        assertEquals(0.5f, holder.state.value.playbackSnapshot.speed)
    }
}
