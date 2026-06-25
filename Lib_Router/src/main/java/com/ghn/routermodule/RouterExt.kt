package com.ghn.routermodule

import android.content.Context
import com.therouter.TheRouter
import com.therouter.router.Navigator

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
 * 描述: 路由扩展工具，负责提供简化的字符串跳转调用方式。
 */
inline fun String.navigate(
    context: Context? = null,
    block: Navigator.() -> Unit = {}
) {
    TheRouter.build(this)
        .apply(block)
        .navigation(context)
}
