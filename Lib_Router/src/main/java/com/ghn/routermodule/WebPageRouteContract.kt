package com.ghn.routermodule

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
 * 描述: 通用 Web 页面路由契约，统一约定路由参数键值。
 */

object WebPageRouteContract {

    fun applyTo(navigator: Navigator, request: WebPageRequest) {
        val effectiveRequest = request.resolveForNavigation()
        navigator.withString(RouterParams.KEY_WBE_URL, effectiveRequest.url)
        navigator.withBoolean(RouterParams.KEY_WEB_ENABLE_BRIDGE, effectiveRequest.enableBridge)
        navigator.withString(
            RouterParams.KEY_WEB_BRIDGE_GROUPS,
            WebBridgeGroups.encode(effectiveRequest.bridgeGroups)
        )
    }

    fun decodeOrNull(
        url: String?,
        enableBridge: Boolean,
        bridgeGroupsRaw: String?
    ): WebPageRequest? {
        val resolvedUrl = url?.trim().orEmpty()
        if (resolvedUrl.isEmpty()) {
            return null
        }
        val hasExplicitBridgeGroups = !bridgeGroupsRaw.isNullOrBlank()
        val decodedGroups = WebBridgeGroups.decode(bridgeGroupsRaw)
        val resolvedEnableBridge = enableBridge && (
            !hasExplicitBridgeGroups || decodedGroups.isNotEmpty()
        )
        return WebPageRequest(
            url = resolvedUrl,
            enableBridge = resolvedEnableBridge,
            bridgeGroups = if (resolvedEnableBridge && hasExplicitBridgeGroups) {
                decodedGroups
            } else {
                emptySet()
            }
        ).normalized()
    }

    fun decode(
        url: String?,
        enableBridge: Boolean,
        bridgeGroupsRaw: String?
    ): WebPageRequest {
        return requireNotNull(
            decodeOrNull(
                url = url,
                enableBridge = enableBridge,
                bridgeGroupsRaw = bridgeGroupsRaw
            )
        ) {
            "WebPage route requires a non-blank url"
        }
    }
}
