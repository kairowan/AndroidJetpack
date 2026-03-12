package com.kt.NetworkModel.helper

import com.kt.NetworkModel.callback.INetworkCallback

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Legacy 网络层全局配置入口
 */
object NetConfigHelper {
    var callback: INetworkCallback? = null

    fun init(callback: INetworkCallback) {
        this.callback = callback
    }
}
