package com.ghn.feature.capture.web

import androidx.fragment.app.FragmentActivity
import com.ghn.lib.base.web.bridge.AnnotatedWebBridgeModule
import com.ghn.lib.base.web.bridge.BridgeHandler
import com.ghn.lib.base.web.bridge.WebBridgeCallback
import com.ghn.routermodule.AppRouter
import com.ghn.routermodule.WebBridgeGroups
import com.ghn.routermodule.aop.capture.NetworkCaptureAccess
import com.ghn.routermodule.feature.FeatureFlagCenter
import com.ghn.routermodule.feature.FeatureKeys
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
 * 描述: 抓包桥接模块，负责向 WebBridge 暴露抓包面板相关能力。
 */
@Single(binds = [com.ghn.lib.base.web.bridge.WebBridgeModule::class])
class CaptureWebBridgeModule : AnnotatedWebBridgeModule() {
    override val group: String = WebBridgeGroups.CAPTURE

    @BridgeHandler("capture.getStatus")
    private fun getStatus(): Map<String, Boolean> {
        return mapOf(
            "enabled" to FeatureFlagCenter.isEnabled(FeatureKeys.NETWORK_CAPTURE),
            "debugOnly" to true
        )
    }

    @BridgeHandler("capture.openPanel")
    @NetworkCaptureAccess
    private fun openPanel(activity: FragmentActivity, callback: WebBridgeCallback) {
        AppRouter.openNetworkCapture(activity)
        callback.success()
    }
}
