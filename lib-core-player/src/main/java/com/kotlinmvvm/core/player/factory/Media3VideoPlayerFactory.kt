package com.kotlinmvvm.core.player.factory

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.kotlinmvvm.core.player.api.VideoPlayerController
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.core.player.engine.Media3VideoPlayerController
import okhttp3.Call

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Media3 播放器默认工厂，将宿主提供的受控 OkHttp 传输接入共享视频缓存和播放引擎
 */
@OptIn(UnstableApi::class)
class Media3VideoPlayerFactory(
    context: Context,
    callFactory: Call.Factory
) : VideoPlayerFactory {
    private val appContext = context.applicationContext
    private val upstreamFactory = OkHttpDataSource.Factory(callFactory)

    override fun create(): VideoPlayerController =
        Media3VideoPlayerController(appContext, upstreamFactory)
}
