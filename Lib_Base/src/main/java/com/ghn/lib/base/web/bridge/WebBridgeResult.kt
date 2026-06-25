package com.ghn.lib.base.web.bridge

import org.json.JSONArray
import org.json.JSONObject

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
 * 描述: WebBridge 结果模型，统一描述桥接调用的执行状态。
 */

object WebBridgeResult {

    fun success(data: Any? = null, message: String = "ok"): String {
        return createPayload(code = 0, message = message, data = data)
    }

    fun failure(message: String, code: Int = -1, data: Any? = null): String {
        return createPayload(code = code, message = message, data = data)
    }

    private fun createPayload(
        code: Int,
        message: String,
        data: Any?
    ): String {
        return JSONObject().apply {
            put("code", code)
            put("message", message)
            put("data", wrapData(data))
        }.toString()
    }

    private fun wrapData(data: Any?): Any? {
        return when (data) {
            null -> JSONObject.NULL
            is JSONObject -> data
            is JSONArray -> data
            is Map<*, *> -> JSONObject(data)
            is Collection<*> -> JSONArray(data)
            else -> JSONObject.wrap(data)
        }
    }
}
