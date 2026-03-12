package com.kotlinmvvm.core.data.eyepetizer

/**
 * @author 浩楠
 *
 * @date 2026-3-9
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: Eyepetizer 共享最小响应模型
 */
internal data class EyepetizerPayloadResponse(
    val itemList: List<EyepetizerPayloadItem> = emptyList(),
    val nextPageUrl: String? = null
)

internal data class EyepetizerPayloadItem(
    val type: String? = null,
    val data: EyepetizerPayloadData? = null
)

internal data class EyepetizerPayloadData(
    val dataType: String? = null,
    val id: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val text: String? = null,
    val playUrl: String? = null,
    val duration: Int? = null,
    val category: String? = null,
    val cover: EyepetizerPayloadCover? = null,
    val author: EyepetizerPayloadAuthor? = null,
    val header: EyepetizerPayloadHeader? = null,
    val itemList: List<EyepetizerPayloadItem> = emptyList()
)

internal data class EyepetizerPayloadCover(
    val feed: String? = null,
    val detail: String? = null
)

internal data class EyepetizerPayloadAuthor(
    val name: String? = null,
    val icon: String? = null
)

internal data class EyepetizerPayloadHeader(
    val title: String? = null
)
