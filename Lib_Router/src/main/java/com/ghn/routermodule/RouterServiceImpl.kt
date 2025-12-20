package com.ghn.routermodule

import com.therouter.TheRouter


/**
 * @author 浩楠
 * @date 2025/5/30 17:10
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 实现类，封装实际跳转逻辑
 */
class RouterServiceImpl : RouterService {

    override fun goToHome() {
        TheRouter.build(RouterPath.Main.HOME).navigation()
    }

    override fun geToKey() {
        TheRouter.build(RouterPath.User.UserKEY).navigation()
    }


    override fun goToProfile(weburl: String) {
        TheRouter.build(RouterPath.Web.WEBVIEW)
            .withString(RouterParams.KEY_WBE_URL, weburl)
            .navigation()
    }

    override fun goToNet() {
        TheRouter.build(RouterPath.Net.NETWORKCAPTURE).navigation()
    }

    override fun goTo(path: String, extras: Map<String, Any?>) {
        val builder = TheRouter.build(path)
        extras.forEach { (key, value) ->
            when (value) {
                is Int -> builder.withInt(key, value)
                is String -> builder.withString(key, value)
                is Boolean -> builder.withBoolean(key, value)
                is Long -> builder.withLong(key, value)
                is Float -> builder.withFloat(key, value)
                is Double -> builder.withDouble(key, value)
                is Char -> builder.withChar(key, value)
            }
        }
        builder.navigation()
    }
}