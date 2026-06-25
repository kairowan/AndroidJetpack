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
 * 描述: 桥接页面策略定义，统一描述 Web 页面接入约束。
 */

fun WebPageRequest.resolveForNavigation(): WebPageRequest {
    val normalizedRequest = normalized()
    if (!normalizedRequest.enableBridge) {
        return normalizedRequest
    }
    if (isTrustedBridgeUrl(normalizedRequest.url, normalizedRequest.bridgeGroups)) {
        return normalizedRequest
    }
    return normalizedRequest.copy(
        enableBridge = false,
        bridgeGroups = emptySet()
    )
}
