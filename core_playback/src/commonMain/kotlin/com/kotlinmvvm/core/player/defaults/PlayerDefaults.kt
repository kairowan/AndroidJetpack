package com.kotlinmvvm.core.player.defaults

/**
 * @author 浩楠
 *
 * @date 2026-3-11
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: KMP 共享播放器默认值
 */
object PlayerDefaults {
    const val SEEK_INTERVAL_MS: Long = 10_000L
    const val PRELOAD_BYTES: Long = 2L * 1024 * 1024
}

@Deprecated(
    message = "Use PlayerDefaults.SEEK_INTERVAL_MS",
    replaceWith = ReplaceWith("PlayerDefaults.SEEK_INTERVAL_MS")
)
const val DEFAULT_SEEK_INTERVAL_MS: Long = PlayerDefaults.SEEK_INTERVAL_MS

@Deprecated(
    message = "Use PlayerDefaults.PRELOAD_BYTES",
    replaceWith = ReplaceWith("PlayerDefaults.PRELOAD_BYTES")
)
const val DEFAULT_PRELOAD_BYTES: Long = PlayerDefaults.PRELOAD_BYTES
