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
 * 描述: WebBridge 回调接口，用于向前端回传处理结果。
 */

interface WebBridgeCallback {

    fun reply(payload: String)

    fun success(data: Any? = null, message: String = "ok") {
        reply(WebBridgeResult.success(data, message))
    }

    fun failure(message: String, code: Int = -1, data: Any? = null) {
        reply(WebBridgeResult.failure(message, code, data))
    }
}
