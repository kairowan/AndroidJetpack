package com.ghn.lib.base.web.bridge

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
 * 描述: WebBridge 注册器接口，负责声明桥接处理器的注册方式。
 */

interface WebBridgeRegistrar {
    fun handler(
        name: String,
        block: (data: String?, callback: WebBridgeCallback) -> Unit
    )

    fun defaultHandler(
        block: (data: String?, callback: WebBridgeCallback) -> Unit
    )
}
