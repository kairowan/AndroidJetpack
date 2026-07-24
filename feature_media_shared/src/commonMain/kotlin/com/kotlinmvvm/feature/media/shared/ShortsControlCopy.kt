package com.kotlinmvvm.feature.media.shared

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Shorts 跨端控制文案，由 Screen 同时用于可见文本和无障碍语义
 */
data class ShortsControlCopy(
    val enterPortraitFullscreenLabel: String,
    val enterLandscapeFullscreenLabel: String,
    val toggleOrientationLabel: String,
    val exitFullscreenLabel: String
)
