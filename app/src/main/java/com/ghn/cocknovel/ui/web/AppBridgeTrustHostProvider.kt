package com.ghn.cocknovel.ui.web

import com.ghn.routermodule.BridgeTrustHostProvider
import com.ghn.routermodule.WebBridgeGroups
import com.kt.network.net.NetworkHost
import com.therouter.inject.ServiceProvider
import com.therouter.inject.Singleton
import java.net.URI

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
 * 描述: 应用桥接白名单提供者，负责输出当前可信宿主配置。
 */

@Singleton
@ServiceProvider(returnType = BridgeTrustHostProvider::class)
class AppBridgeTrustHostProvider : BridgeTrustHostProvider {

    override fun trustedHosts(group: String): Set<String> {
        return when (group) {
            WebBridgeGroups.CORE -> CORE_TRUSTED_HOSTS
            WebBridgeGroups.AUTH -> AUTH_TRUSTED_HOSTS
            WebBridgeGroups.CAPTURE -> LOCAL_TRUSTED_HOSTS
            else -> emptySet()
        }
    }

    companion object {
        private val LOCAL_TRUSTED_HOSTS: Set<String> = setOf("localhost", "127.0.0.1")

        private val CORE_TRUSTED_HOSTS: Set<String> = networkHosts(
            NetworkHost.LOGIN,
            NetworkHost.TEST,
            NetworkHost.VIDEO,
            NetworkHost.DEGREE,
            NetworkHost.WAN_ANDROID,
            NetworkHost.SURVEY
        ) + LOCAL_TRUSTED_HOSTS

        private val AUTH_TRUSTED_HOSTS: Set<String> = CORE_TRUSTED_HOSTS

        private fun networkHosts(vararg hosts: NetworkHost): Set<String> {
            return buildSet {
                hosts.mapNotNullTo(this) { host ->
                    runCatching { URI(host.baseUrl).host?.trim() }.getOrNull()
                }
            }
        }
    }
}
