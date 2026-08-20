package com.kotlinmvvm.app.config

import com.kotlinmvvm.core.network.config.NetworkEndpoint

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用远程服务地址注册表，每个 BaseUrl 只在此处声明一次并使用明确业务名称
 */
object AppNetworkEndpoints {
    /**
     * 返回当前环境的信息流服务地址；其他业务服务应新增独立语义化函数，不能复用 Feed 名称。
     */
    fun feed(environment: AppEnvironment) = NetworkEndpoint(
        name = "feed-${environment.name.lowercase()}",
        baseUrl = when (environment) {
            // ponytail: 示例服务没有公开的开发/预发 Host；接入自有后端时只替换这里的两项。
            AppEnvironment.DEVELOPMENT,
            AppEnvironment.STAGING,
            AppEnvironment.PRODUCTION -> "https://baobab.kaiyanapp.com/"
        }
    )
}
