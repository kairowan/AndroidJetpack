package com.kotlinmvvm.feature.shorts.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.ui.ShortVideoOverlay
import com.kotlinmvvm.core.player.ui.ShortVideoPager
import com.kotlinmvvm.core.player.model.VideoWindowMode
import com.kotlinmvvm.feature.shorts.R
import com.kotlinmvvm.feature.shorts.ui.model.ShortsVideoPlayerItem

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 短视频分页播放内容，保存当前页、触发临近分页并组合视频信息浮层
 */
@Composable
fun ShortsFeed(
    videos: List<FeedVideo>,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    player: VideoPlayerController,
    windowMode: VideoWindowMode,
    onWindowModeChanged: (VideoWindowMode) -> Unit,
    onLoadMore: () -> Unit
) {
    var savedPageIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = savedPageIndex.coerceIn(0, videos.lastIndex),
        pageCount = { videos.size }
    )
    val playerItems = remember(videos) { videos.map(::ShortsVideoPlayerItem) }

    ShortVideoPager(
        items = playerItems,
        pagerState = pagerState,
        player = player,
        onPageChanged = { page ->
            savedPageIndex = page
            if (page >= videos.lastIndex - 2 && canLoadMore && !isLoadingMore) onLoadMore()
        }
    ) { item, isCurrent ->
        ShortVideoOverlay(Modifier.align(Alignment.BottomCenter)) {
            val video = item.video
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = video.authorIconUrl,
                    contentDescription = stringResource(R.string.shorts_author_avatar, video.authorName),
                    modifier = Modifier.size(40.dp)
                        .background(Color.Gray, MaterialTheme.shapes.extraLarge)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.shorts_author, video.authorName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(video.title, style = MaterialTheme.typography.bodyLarge, color = Color.White, maxLines = 2)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.shorts_category, video.category),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            if (isCurrent) {
                Spacer(Modifier.height(8.dp))
                ShortsFullscreenActions(windowMode, onWindowModeChanged)
            }
        }
    }
}
