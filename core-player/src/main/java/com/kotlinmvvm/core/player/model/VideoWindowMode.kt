package com.kotlinmvvm.core.player.model

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 播放页面请求的窗口展示模式，不包含 Activity 或系统栏等宿主实现
 */
enum class VideoWindowMode {
    INLINE,
    PORTRAIT_FULLSCREEN,
    LANDSCAPE_FULLSCREEN
}
