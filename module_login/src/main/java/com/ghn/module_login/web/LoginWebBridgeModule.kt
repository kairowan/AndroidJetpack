package com.ghn.module_login.web

import androidx.fragment.app.FragmentActivity
import com.ghn.lib.base.aop.confirm.ConfirmAction
import com.ghn.lib.base.web.bridge.AnnotatedWebBridgeModule
import com.ghn.lib.base.web.bridge.BridgeHandler
import com.ghn.lib.base.web.bridge.WebBridgeCallback
import com.ghn.routermodule.AppRouter
import com.ghn.routermodule.WebBridgeGroups
import com.ghn.routermodule.auth.LoginRequiredActionCenter
import com.ghn.routermodule.auth.LoginSession
import org.koin.core.annotation.Single

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
 * 描述: 登录桥接模块，负责向 WebBridge 暴露登录态相关能力。
 */
@Single(binds = [com.ghn.lib.base.web.bridge.WebBridgeModule::class])
class LoginWebBridgeModule : AnnotatedWebBridgeModule() {
    override val group: String = WebBridgeGroups.AUTH

    @BridgeHandler("auth.openLogin")
    private fun openLogin(activity: FragmentActivity) {
        AppRouter.openLogin(context = activity)
    }

    @BridgeHandler("auth.getLoginState")
    private fun getLoginState(): Map<String, Boolean> {
        return mapOf(
            "isLoggedIn" to LoginSession.isLoggedIn()
        )
    }

    @BridgeHandler("auth.logout")
    @ConfirmAction(
        title = "退出登录",
        message = "确定清除当前登录态吗？"
    )
    private fun logout(activity: FragmentActivity, callback: WebBridgeCallback) {
        LoginRequiredActionCenter.clearPendingAction()
        LoginSession.clear()
        callback.success(message = "logout success")
    }
}
