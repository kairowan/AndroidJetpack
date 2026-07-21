package com.kotlinmvvm.data.feed.remote.model

/**
 * @author 浩楠
 * @date 2026/7/21 12:56
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Feed 接口的条目载荷传输模型，兼容视频、文本和嵌套集合结构
 */
internal data class FeedDataDto(
    val dataType: String? = null,
    val id: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val text: String? = null,
    val playUrl: String? = null,
    val duration: Int? = null,
    val category: String? = null,
    val cover: FeedCoverDto? = null,
    val author: FeedAuthorDto? = null,
    val header: FeedHeaderDto? = null,
    val itemList: List<FeedItemDto> = emptyList()
)
