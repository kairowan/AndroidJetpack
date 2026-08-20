package com.kotlinmvvm.core.player.engine

import android.os.Looper

/** 在触碰 Media3 状态前校验当前线程，避免跨线程访问播放器产生竞态。 */
internal fun ensurePlayerMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) { "播放器只能在主线程访问" }
}
