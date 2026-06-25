package com.ghn.cocknovel.ui.web

import androidx.fragment.app.FragmentActivity
import com.ghn.lib.base.aop.permission.capability.RequireCameraPermission
import com.ghn.lib.base.web.bridge.AnnotatedWebBridgeModule
import com.ghn.lib.base.web.bridge.BridgeHandler
import com.ghn.lib.base.web.bridge.WebBridgeCallback
import com.ghn.lib.base.web.bridge.WebBridgeHost
import com.ghn.lib.base.web.bridge.WebBridgeResponse
import com.ghn.routermodule.AppRouter
import com.ghn.routermodule.WebBridgeGroups
import com.ghn.routermodule.auth.LoginRequired
import com.ghn.routermodule.auth.LoginSession
import com.ghn.routermodule.isTrustedBridgeUrl
import org.json.JSONArray
import org.json.JSONObject
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
 * 描述: 应用桥接模块，负责注册应用层自定义 WebBridge 能力。
 */

@Single(binds = [com.ghn.lib.base.web.bridge.WebBridgeModule::class])
class AppWebBridgeModule : AnnotatedWebBridgeModule() {
    override val group: String = WebBridgeGroups.CORE

    @BridgeHandler("app.closePage")
    private fun closePage(host: WebBridgeHost, callback: WebBridgeCallback) {
        callback.success()
        host.closePage()
    }

    @BridgeHandler("app.getContext")
    private fun getContext(host: WebBridgeHost): Map<String, Any> {
        return mapOf(
            "packageName" to host.activity.packageName,
            "currentUrl" to host.currentUrl().orEmpty(),
            "isLoggedIn" to LoginSession.isLoggedIn()
        )
    }

    @BridgeHandler("router.openHome")
    private fun openHome(activity: FragmentActivity) {
        AppRouter.openHome(activity)
    }

    @BridgeHandler("router.openSettings")
    private fun openSettings(activity: FragmentActivity) {
        AppRouter.openSettings(activity)
    }

    @BridgeHandler("router.openUserKey")
    @LoginRequired(message = "请先登录后再继续")
    private fun openUserKey(activity: FragmentActivity, callback: WebBridgeCallback) {
        AppRouter.openUserKey(activity)
        callback.success()
    }

    @BridgeHandler("router.openUrl")
    private fun openUrl(host: WebBridgeHost, data: String?): Any {
        val payload = data.toOpenWebPayloadOrNull(
            defaultGroups = host.enabledBridgeGroups.ifEmpty { setOf(WebBridgeGroups.CORE) }
        )
        if (payload == null) {
            return WebBridgeResponse.failure("url 或 bridgeGroups 配置无效")
        }
        if (payload.enableBridge && !isTrustedBridgeUrl(payload.url, payload.bridgeGroups)) {
            return WebBridgeResponse.failure("当前域名不允许启用 bridge")
        }
        if (payload.enableBridge) {
            AppRouter.openBridgeWeb(payload.url, payload.bridgeGroups, host.activity)
        } else {
            AppRouter.openPlainWeb(payload.url, host.activity)
        }
        return mapOf(
            "url" to payload.url,
            "enableBridge" to payload.enableBridge,
            "bridgeGroups" to payload.bridgeGroups.toList()
        )
    }

    @BridgeHandler("permission.requestCamera")
    @RequireCameraPermission(tag = "web_bridge_camera")
    private fun requestCameraPermission(activity: FragmentActivity, callback: WebBridgeCallback) {
        callback.success(message = "camera permission granted")
    }
}

private data class OpenWebPayload(
    val url: String,
    val enableBridge: Boolean,
    val bridgeGroups: Set<String>
)

private fun String?.toOpenWebPayloadOrNull(defaultGroups: Set<String>): OpenWebPayload? {
    if (this.isNullOrBlank()) {
        return null
    }
    if (startsWith("http://") || startsWith("https://")) {
        return OpenWebPayload(
            url = this,
            enableBridge = false,
            bridgeGroups = emptySet()
        )
    }
    return runCatching {
        JSONObject(this).toOpenWebPayload(defaultGroups)
    }.getOrNull()
}

private fun JSONObject.toOpenWebPayload(defaultGroups: Set<String>): OpenWebPayload? {
    val url = optString("url").takeIf { it.isNotBlank() } ?: return null
    val parsedGroups = parseBridgeGroups(defaultGroups)
    val enableBridge = if (has("enableBridge")) {
        optBoolean("enableBridge")
    } else {
        parsedGroups.groups.isNotEmpty()
    }
    if (enableBridge && parsedGroups.explicit && parsedGroups.groups.isEmpty()) {
        return null
    }
    return OpenWebPayload(
        url = url,
        enableBridge = enableBridge,
        bridgeGroups = if (enableBridge) parsedGroups.groups else emptySet()
    )
}

private data class ParsedBridgeGroups(
    val groups: Set<String>,
    val explicit: Boolean
)

private fun JSONObject.parseBridgeGroups(defaultGroups: Set<String>): ParsedBridgeGroups {
    val rawGroups = opt("bridgeGroups")
    if (rawGroups == null || rawGroups == JSONObject.NULL) {
        return ParsedBridgeGroups(
            groups = WebBridgeGroups.sanitize(defaultGroups)
                .ifEmpty { WebBridgeGroups.CORE_DEFAULT },
            explicit = false
        )
    }
    val groups = when (rawGroups) {
        is JSONArray -> buildSet {
            for (index in 0 until rawGroups.length()) {
                rawGroups.optString(index)
                    ?.let(::add)
            }
        }

        is String -> rawGroups
            .split(',')
            .toSet()

        else -> emptySet()
    }
    return ParsedBridgeGroups(
        groups = WebBridgeGroups.sanitize(groups),
        explicit = true
    )
}
