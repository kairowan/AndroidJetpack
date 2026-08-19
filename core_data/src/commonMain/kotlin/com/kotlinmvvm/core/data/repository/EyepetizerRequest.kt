package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.model.EyepetizerFeedSource

/**
 * @author 浩楠
 * @date 2026/7/24 13:58
 * 描述: 经过安全策略校验的 Eyepetizer 请求快照
 */
internal data class EyepetizerRequest(
    val source: EyepetizerFeedSource,
    val url: String
)
