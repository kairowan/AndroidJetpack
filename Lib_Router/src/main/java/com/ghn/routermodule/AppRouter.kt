package com.ghn.routermodule

import android.content.Context
import com.ghn.routermodule.aop.capture.NetworkCaptureAccess
import com.ghn.routermodule.aop.feature.FeatureEnabled
import com.ghn.routermodule.aop.guard.PreventRepeat
import com.ghn.routermodule.auth.LoginRequired
import com.ghn.routermodule.feature.FeatureKeys

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

    fun openHome(context: Context? = null) {
        requireRouterService<MainPageRouter>().openHome(context)
    }

    @LoginRequired(message = "请先登录后再查看用户信息")
    @PreventRepeat(
        intervalMillis = 1200L,
        key = "router_open_user_key",
        message = "页面打开中，请勿重复操作"
    )
    fun openUserKey(context: Context? = null) {
        requireRouterService<UserPageRouter>().openUserKey(context)
    }

    @PreventRepeat(
        intervalMillis = 800L,
        key = "router_open_settings",
        message = "页面打开中，请勿重复操作"
    )
    fun openSettings(context: Context? = null) {
        requireRouterService<SettingPageRouter>().openSettings(context)
    }

    @FeatureEnabled(
        featureKey = FeatureKeys.FONT_SETTINGS,
        message = "字体设置功能暂未开放"
    )
    @PreventRepeat(
        intervalMillis = 800L,
        key = "router_open_font_settings",
        message = "页面打开中，请勿重复操作"
    )
    fun openFontSettings(context: Context? = null) {
        requireRouterService<SettingPageRouter>().openFontSettings(context)
    }

    fun openWeb(
        url: String,
        enableBridge: Boolean = false,
        bridgeGroups: Set<String> = emptySet(),
        context: Context? = null
    ) {
        if (!enableBridge && bridgeGroups.isEmpty()) {
            openPlainWeb(url, context)
            return
        }
        openWeb(
            WebPageRequest(
                url = url,
                enableBridge = enableBridge,
                bridgeGroups = bridgeGroups
            ),
            context
        )
    }

    fun openPlainWeb(url: String, context: Context? = null) {
        openWeb(WebPageRequest.normal(url), context)
    }

    fun openWeb(request: WebPageRequest, context: Context? = null) {
        requireRouterService<WebPageRouter>().open(request.resolveForNavigation(), context)
    }

    fun openBridgeWeb(url: String, context: Context? = null) {
        requireRouterService<CoreBridgeWebRouter>().open(url, context = context)
    }

    fun openBridgeWeb(
        url: String,
        bridgeGroups: Set<String>,
        context: Context? = null
    ) {
        openWeb(WebPageRequest.bridge(url, bridgeGroups), context)
    }

    fun openBridgeWeb(
        url: String,
        vararg bridgeGroups: String
    ) {
        openBridgeWeb(url, bridgeGroups.toSet())
    }

    fun openAuthWeb(
        url: String,
        extraGroups: Set<String> = emptySet(),
        context: Context? = null
    ) {
        requireRouterService<AuthBridgeWebRouter>().open(url, extraGroups, context)
    }

    fun openAuthWeb(url: String, vararg extraGroups: String) {
        openAuthWeb(url, extraGroups.toSet())
    }

    fun openCaptureWeb(
        url: String,
        extraGroups: Set<String> = emptySet(),
        context: Context? = null
    ) {
        requireRouterService<CaptureBridgeWebRouter>().open(url, extraGroups, context)
    }

    fun openCaptureWeb(url: String, vararg extraGroups: String) {
        openCaptureWeb(url, extraGroups.toSet())
    }

    @NetworkCaptureAccess
    @PreventRepeat(
        intervalMillis = 800L,
        key = "router_open_network_capture",
        message = "页面打开中，请勿重复操作"
    )
    fun openNetworkCapture(context: Context? = null) {
        requireRouterService<NetworkCaptureRouter>().openNetworkCapture(context)
    }

    @PreventRepeat(
        intervalMillis = 1200L,
        key = "router_open_login",
        message = "登录页打开中，请勿重复操作"
    )
    fun openLogin(loginRequestId: String? = null, context: Context? = null) {
        requireRouterService<LoginRouter>().openLogin(loginRequestId, context)
    }
}
