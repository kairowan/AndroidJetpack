package com.kotlinmvvm.core.player.facade

import android.content.Context
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.cache.VideoCacheStore
import com.kotlinmvvm.core.player.engine.VideoPlayer

/**
 * 播放器创建入口。
 */
object PlayerFactory {

    @JvmStatic
    fun create(context: Context): IPlayer {
        val appContext = context.applicationContext
        val cacheStore = VideoCacheStore(appContext)
        return VideoPlayer(appContext, cacheStore)
    }
}
