package com.kotlinmvvm.core.player.defaults

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 播放引擎通用默认值，统一快进快退间隔和预加载数据量
 */
object PlayerDefaults {
    const val SEEK_INTERVAL_MS: Long = 10_000L
    const val PRELOAD_BYTES: Long = 2L * 1024 * 1024
    const val MAX_CONCURRENT_PRELOADS: Int = 2
}
