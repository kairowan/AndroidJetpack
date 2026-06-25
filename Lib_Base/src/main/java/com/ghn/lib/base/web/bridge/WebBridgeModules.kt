package com.ghn.lib.base.web.bridge

import com.ghn.lib.base.aop.TraceTime
import org.koin.core.context.GlobalContext

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
 * 描述: WebBridge 模块发现工具，负责聚合并输出可用桥接模块。
 */

object WebBridgeModules {

    @TraceTime("web_bridge_modules_resolve", warnAtMillis = 8L)
    fun resolve(
        url: String? = null,
        enabledGroups: Set<String> = emptySet()
    ): List<WebBridgeModule> {
        val koin = GlobalContext.getOrNull() ?: return emptyList()
        return runCatching {
            val modules = koin.getAll<WebBridgeModule>()
                .distinctBy { it::class.java.name }
                .filter { enabledGroups.isEmpty() || enabledGroups.contains(it.group) }
                .filter { it.shouldRegister(url) }
            val duplicateHandlers = modules
                .flatMap { module ->
                    module.handlerNames().map { handlerName ->
                        handlerName to module::class.java.name
                    }
                }
                .groupBy(keySelector = { it.first }, valueTransform = { it.second })
                .filterValues { providers -> providers.size > 1 }
            check(duplicateHandlers.isEmpty()) {
                duplicateHandlers.entries.joinToString(
                    prefix = "Duplicate bridge handler names found: "
                ) { (handlerName, providers) ->
                    "$handlerName -> ${providers.joinToString()}"
                }
            }
            modules
        }.getOrElse { throwable ->
            if (throwable is IllegalStateException &&
                throwable.message?.startsWith("Duplicate bridge handler names found:") == true
            ) {
                throw throwable
            }
            emptyList()
        }
    }
}
