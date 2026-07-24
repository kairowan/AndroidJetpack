package com.kotlinmvvm.core.player.facade

import android.content.Context
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.core.player.cache.VideoCacheStore
import com.kotlinmvvm.core.player.engine.VideoPlayer

/**
 * @author 浩楠
 * @date 2026/7/24 13:29
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Android 默认播放器工厂，由应用进程唯一持有并复用一个视频缓存
 */
internal class DefaultVideoPlayerFactory(
    context: Context
) : VideoPlayerFactory {
    private val appContext = context.applicationContext
    private val cacheStore = VideoCacheStore(appContext)

    override fun create(): IPlayer = VideoPlayer(appContext, cacheStore)
}

/**
 * 创建应用进程级播放器工厂。
 */
fun createVideoPlayerFactory(context: Context): VideoPlayerFactory {
    return DefaultVideoPlayerFactory(context.applicationContext)
}
