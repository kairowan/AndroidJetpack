package com.ghn.lib.base.web.bridge

import androidx.fragment.app.FragmentActivity

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
 * 描述: WebBridge 宿主接口，向桥接模块暴露上下文和能力访问入口。
 */

interface WebBridgeHost {
    val activity: FragmentActivity
    val enabledBridgeGroups: Set<String>

    fun currentUrl(): String?

    fun closePage()

    fun callHandler(
        handlerName: String,
        data: String? = null,
        callback: ((String) -> Unit)? = null
    )

    fun sendToWeb(
        data: String,
        callback: ((String) -> Unit)? = null
    )
}
