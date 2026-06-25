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
 * 描述: WebBridge 分组常量定义，统一维护桥接模块分组标识。
 */

object WebBridgeGroups {
    const val CORE = "core"
    const val AUTH = "auth"
    const val CAPTURE = "capture"
    val SUPPORTED: Set<String> = setOf(CORE, AUTH, CAPTURE)
    val CORE_DEFAULT: Set<String> = setOf(CORE)
    val AUTH_DEFAULT: Set<String> = setOf(CORE, AUTH)
    val CAPTURE_DEFAULT: Set<String> = setOf(CORE, CAPTURE)

    fun encode(groups: Set<String>): String {
        return sanitize(groups)
            .joinToString(",")
    }

    fun decode(raw: String?): Set<String> {
        return sanitize(
            raw
            .orEmpty()
            .split(',')
            .toSet()
        )
    }

    fun sanitize(groups: Set<String>): Set<String> {
        return groups
            .asSequence()
            .map { it.trim().lowercase() }
            .filter { it in SUPPORTED }
            .toSet()
    }
}
