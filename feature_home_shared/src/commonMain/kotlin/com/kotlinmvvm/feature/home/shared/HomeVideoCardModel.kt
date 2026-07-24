package com.kotlinmvvm.feature.home.shared

/**
 * @author 浩楠
 * @date 2026/7/24 13:50
 * 描述: 首页共享视频卡片展示模型
 */
data class HomeVideoCardModel(
    val id: Int,
    val title: String,
    val descriptionText: String,
    val subtitle: String,
    val authorName: String,
    val authorHandle: String,
    val categoryDurationLabel: String,
    val authorIcon: String,
    val category: String,
    val duration: Int,
    val coverUrl: String,
    val playUrl: String
)
