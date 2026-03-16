package com.ghn.routermodule

import com.therouter.TheRouter

/**
 * @author 浩楠
 * @date 2025/5/30 17:11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 面向业务层的导航入口，只暴露语义化跳转
 */
object AppRouter {

    fun openHome() {
        navigate(RouterPath.Main.HOME)
    }

    fun openUserKey() {
        navigate(RouterPath.User.UserKEY)
    }

    fun openWeb(url: String) {
        RouterPath.Web.WEBVIEW.navigate {
            withString(RouterParams.KEY_WBE_URL, url)
        }
    }

    fun openNetworkCapture() {
        requireService<NetworkCaptureRouter>().openNetworkCapture()
    }

    fun openLogin() {
        requireService<LoginRouter>().openLogin()
    }

    private inline fun <reified T : Any> requireService(): T {
        return requireNotNull(TheRouter.get(T::class.java)) {
            "TheRouter provider missing for ${T::class.java.name}"
        }
    }

    private fun navigate(path: String) {
        path.navigate()
    }
}
