package com.kotlinmvvm.data.feed.remote.model

/**
 * @author 浩楠
 * @date 2026/7/21 12:56
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Feed 接口的多态条目传输模型，通过类型字段解释可空载荷
 */
internal data class FeedItemDto(
    val type: String? = null,
    val data: FeedDataDto? = null
)
