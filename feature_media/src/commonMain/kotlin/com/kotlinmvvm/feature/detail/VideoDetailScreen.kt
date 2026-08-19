package com.kotlinmvvm.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * @author 浩楠
 * @date 2026/7/24 15:02
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Android、iOS、Desktop 与 Web 共用的详情页纯 UI，通过插槽接收平台播放器和图片
 */
@Composable
fun VideoDetailScreen(
    pageModel: VideoDetailPageModel,
    player: @Composable (isFullscreen: Boolean, modifier: Modifier) -> Unit,
    authorImage: @Composable (
        url: String,
        contentDescription: String,
        modifier: Modifier
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pageModel.isFullscreen) {
        player(true, modifier.fillMaxSize())
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        player(false, Modifier)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = pageModel.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = pageModel.metadataLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                authorImage(
                    pageModel.authorIcon,
                    pageModel.authorName,
                    Modifier
                        .size(48.dp)
                        .background(Color.Gray, shape = MaterialTheme.shapes.small)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = pageModel.authorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (pageModel.descriptionText.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = pageModel.descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
