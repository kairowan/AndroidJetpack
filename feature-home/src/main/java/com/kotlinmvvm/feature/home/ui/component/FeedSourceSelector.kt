package com.kotlinmvvm.feature.home.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kotlinmvvm.domain.feed.model.FeedSource
import com.kotlinmvvm.feature.home.R

/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 首页信息流来源选择组件，只负责展示选中项并回传新的来源
 */
@Composable
fun FeedSourceSelector(
    selectedSource: FeedSource,
    onSelected: (FeedSource) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FeedSource.entries.forEach { source ->
            FilterChip(
                selected = source == selectedSource,
                onClick = { onSelected(source) },
                label = { Text(stringResource(source.titleResource)) }
            )
        }
    }
}

@get:StringRes
private val FeedSource.titleResource: Int
    get() = when (this) {
        FeedSource.HOME_SELECTED -> R.string.home_source_selected
        FeedSource.DISCOVERY -> R.string.home_source_discovery
        FeedSource.FOLLOW -> R.string.home_source_follow
        FeedSource.DISCOVERY_HOT -> R.string.home_source_hot
        FeedSource.DISCOVERY_CATEGORY -> R.string.home_source_category
        FeedSource.AUTHORS -> R.string.home_source_authors
    }
