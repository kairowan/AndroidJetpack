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
 * 描述: WebBridge 响应模型，统一描述桥接层的回传数据。
 */

sealed class WebBridgeResponse {

    data class Success(
        val data: Any? = null,
        val message: String = "ok"
    ) : WebBridgeResponse()

    data class Failure(
        val message: String,
        val code: Int = -1,
        val data: Any? = null
    ) : WebBridgeResponse()

    companion object {
        fun success(data: Any? = null, message: String = "ok"): WebBridgeResponse {
            return Success(data = data, message = message)
        }

        fun failure(message: String, code: Int = -1, data: Any? = null): WebBridgeResponse {
            return Failure(message = message, code = code, data = data)
        }
    }
}
