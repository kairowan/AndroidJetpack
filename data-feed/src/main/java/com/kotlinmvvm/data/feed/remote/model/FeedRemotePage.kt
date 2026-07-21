package com.kotlinmvvm.data.feed.remote.model

import com.kotlinmvvm.domain.feed.model.FeedPage

/**
 * @author 浩楠
 * @date 2026/7/21 13:35
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 数据层远程分页结果，在仓库边界内保存服务端下一页地址而不污染领域模型
 */
internal data class FeedRemotePage(
    val page: FeedPage,
    val nextPageUrl: String?
)
