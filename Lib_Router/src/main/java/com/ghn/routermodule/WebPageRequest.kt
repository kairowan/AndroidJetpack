package com.ghn.routermodule

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
 * 描述: 通用 Web 页面请求模型，描述页面打开所需参数。
 */

data class WebPageRequest(
    val url: String,
    val enableBridge: Boolean = false,
    val bridgeGroups: Set<String> = emptySet()
) {

    fun normalized(): WebPageRequest {
        val sanitizedGroups = WebBridgeGroups.sanitize(bridgeGroups)
        val resolvedEnableBridge = when {
            !enableBridge -> false
            bridgeGroups.isNotEmpty() -> sanitizedGroups.isNotEmpty()
            else -> true
        }
        val resolvedGroups = when {
            !resolvedEnableBridge -> emptySet()
            sanitizedGroups.isNotEmpty() -> sanitizedGroups
            else -> WebBridgeGroups.CORE_DEFAULT
        }
        return copy(
            enableBridge = resolvedEnableBridge,
            bridgeGroups = resolvedGroups
        )
    }

    companion object {
        fun normal(url: String): WebPageRequest {
            return WebPageRequest(url = url)
        }

        fun bridge(url: String, bridgeGroups: Set<String>): WebPageRequest {
            return WebPageRequest(
                url = url,
                enableBridge = true,
                bridgeGroups = bridgeGroups
            )
        }
    }
}
