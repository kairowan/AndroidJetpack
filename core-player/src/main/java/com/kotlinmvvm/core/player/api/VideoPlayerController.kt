package com.kotlinmvvm.core.player.api

import androidx.media3.common.Player
import androidx.annotation.MainThread
import com.kotlinmvvm.core.player.defaults.PlayerDefaults
import kotlinx.coroutines.flow.StateFlow

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 面向 Compose 与业务模块的播放器控制契约，隐藏 Media3 引擎实现并暴露只读状态流
 */
interface VideoPlayerController {
    /** 播放器只读状态流，供 Compose 按生命周期收集。 */
    val state: StateFlow<PlayerState>

    /** Media3 UI 渲染层所需的播放器契约，不应由业务代码直接控制。 */
    @get:MainThread
    val media3Player: Player

    /** 按需加载地址，并根据 [autoPlay] 决定是否立即发起播放。 */
    @MainThread
    fun play(url: String, autoPlay: Boolean = true)

    /** 暂停当前媒体但保留播放位置。 */
    @MainThread
    fun pause()

    /** 从当前播放位置继续播放。 */
    @MainThread
    fun resume()

    /** 根据当前播放请求状态切换播放或暂停。 */
    @MainThread
    fun toggle()

    /** 停止当前媒体并重置播放位置。 */
    @MainThread
    fun stop()

    /** 释放播放器及其持有的系统资源，释放后不得继续调用。 */
    @MainThread
    fun release()

    /** 跳转到指定毫秒位置，超出范围时由实现约束到有效区间。 */
    @MainThread
    fun seekTo(ms: Long)

    /** 按 0 到 1 的播放进度比例跳转。 */
    @MainThread
    fun seekTo(progress: Float)

    /** 在当前位置基础上快进指定毫秒。 */
    @MainThread
    fun forward(ms: Long = PlayerDefaults.SEEK_INTERVAL_MS)

    /** 在当前位置基础上后退指定毫秒。 */
    @MainThread
    fun rewind(ms: Long = PlayerDefaults.SEEK_INTERVAL_MS)

    /** 设置播放倍速，非法值由实现拒绝或约束。 */
    @MainThread
    fun setSpeed(speed: Float)

    /** 设置 0 到 1 之间的播放音量。 */
    @MainThread
    fun setVolume(volume: Float)

    /** 预加载单个视频的前 [bytes] 字节到共享缓存。 */
    @MainThread
    fun preload(url: String, bytes: Long = PlayerDefaults.PRELOAD_BYTES)

    /** 用最新地址集合替换过期预加载，并以有界并发缓存每个视频前 [bytes] 字节。 */
    @MainThread
    fun preload(urls: List<String>, bytes: Long = PlayerDefaults.PRELOAD_BYTES)
}
