package com.ghn.lib.base.web.bridge

import com.ghn.routermodule.WebBridgeGroups

/**
 * @author 浩楠
 *
 * @date 2026/6/24
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: WebBridge 模块接口，约定桥接模块的注册入口。
 */

interface WebBridgeModule {
    val group: String
        get() = WebBridgeGroups.CORE

    fun shouldRegister(url: String? = null): Boolean = true

    fun handlerNames(): Set<String> = emptySet()

    fun register(registrar: WebBridgeRegistrar, host: WebBridgeHost)
}
