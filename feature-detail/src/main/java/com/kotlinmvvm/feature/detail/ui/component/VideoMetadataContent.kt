package com.kotlinmvvm.feature.detail.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kotlinmvvm.core.designsystem.theme.AppSpacing
import com.kotlinmvvm.domain.feed.model.FeedItem
import com.kotlinmvvm.domain.feed.model.FeedVideo
import com.kotlinmvvm.core.player.api.formatPlaybackTime
import com.kotlinmvvm.feature.detail.R

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情元数据组件，展示标题、分类、时长、作者和内容描述
 */
@Composable
fun VideoMetadataContent(video: FeedVideo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large)
    ) {
        Text(video.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(AppSpacing.small))
        Text(
            text = stringResource(
                R.string.detail_metadata,
                video.category,
                formatPlaybackTime(video.durationSeconds.toLong() * 1_000L)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.size(AppSpacing.large))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = video.authorIconUrl,
                contentDescription = stringResource(R.string.detail_author_avatar, video.authorName),
                modifier = Modifier.size(48.dp)
                    .background(Color.Gray, shape = MaterialTheme.shapes.small)
            )
            Spacer(Modifier.width(AppSpacing.medium))
            Text(
                video.authorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        if (video.description.isNotEmpty()) {
            Spacer(Modifier.size(AppSpacing.large))
            Text(
                video.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
