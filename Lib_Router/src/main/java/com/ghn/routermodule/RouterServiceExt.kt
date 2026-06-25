package com.ghn.routermodule

import com.therouter.TheRouter

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
 * 描述: RouterServiceExt 类型定义，负责承载对应模块中的基础能力或数据结构。
 */

inline fun <reified T : Any> routerServiceOrNull(): T? {
    return TheRouter.get(T::class.java)
}

inline fun <reified T : Any> requireRouterService(): T {
    return requireNotNull(routerServiceOrNull<T>()) {
        "TheRouter provider missing for ${T::class.java.name}"
    }
}
