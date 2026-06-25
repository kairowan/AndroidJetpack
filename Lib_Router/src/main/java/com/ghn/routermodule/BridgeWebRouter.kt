package com.ghn.routermodule

import android.content.Context

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
 * 描述: 桥接页面路由能力接口，负责封装桥接 Web 页的打开方式。
 */

interface BridgeWebRouter {
    val defaultBridgeGroups: Set<String>

    fun open(url: String, context: Context? = null) {
        open(url, emptySet(), context)
    }

    fun open(
        url: String,
        extraGroups: Set<String>,
        context: Context? = null
    ) {
        AppRouter.openBridgeWeb(url, defaultBridgeGroups + extraGroups, context)
    }
}
