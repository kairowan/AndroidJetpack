package com.ghn.routermodule

import android.net.Uri

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
 * 描述: 桥接白名单提供者接口，负责输出可信宿主列表。
 */

interface BridgeTrustHostProvider {
    fun trustedHosts(group: String): Set<String>

    fun isTrusted(
        host: String,
        bridgeGroups: Set<String> = emptySet()
    ): Boolean {
        val normalizedGroups = WebBridgeGroups.sanitize(
            bridgeGroups.ifEmpty { WebBridgeGroups.CORE_DEFAULT }
        )
        if (normalizedGroups.isEmpty()) {
            return false
        }
        return normalizedGroups.all { group ->
            trustedHosts(group)
                .asSequence()
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .any { trustedHost ->
                    host == trustedHost || host.endsWith(".$trustedHost")
                }
        }
    }
}

fun isTrustedBridgeUrl(
    url: String,
    bridgeGroups: Set<String> = emptySet()
): Boolean {
    val provider = routerServiceOrNull<BridgeTrustHostProvider>() ?: return false
    val host = runCatching {
        Uri.parse(url).host
            ?.trim()
            ?.lowercase()
    }.getOrNull().orEmpty()
    if (host.isBlank()) {
        return false
    }
    return provider.isTrusted(host, bridgeGroups)
}
