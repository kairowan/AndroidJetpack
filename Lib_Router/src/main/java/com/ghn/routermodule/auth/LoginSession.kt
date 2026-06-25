package com.ghn.routermodule.auth

import com.ghn.commonmodule.ext.MVUtils
import com.ghn.routermodule.RouterParams

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
 * 描述: LoginSession 类型定义，负责承载对应模块中的基础能力或数据结构。
 */

object LoginSession {

    fun token(): String = MVUtils.getString(RouterParams.KEY_TOKEN, "").orEmpty()

    fun isLoggedIn(): Boolean = token().isNotBlank()

    fun updateToken(token: String?) {
        if (token.isNullOrBlank()) {
            clear()
            return
        }
        MVUtils.put(RouterParams.KEY_TOKEN, token)
    }

    fun clear() {
        MVUtils.removeKey(RouterParams.KEY_TOKEN)
    }
}
