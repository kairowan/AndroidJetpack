package com.kotlinmvvm.core.player.facade

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * @author 浩楠
 *
 * @date 2026-7-24 14:01
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 生命周期与播放器绑定句柄
 */
class PlayerLifecycleBinding internal constructor(
    private val lifecycle: Lifecycle,
    private val observer: LifecycleEventObserver
) {
    fun unbind() {
        lifecycle.removeObserver(observer)
    }
}
