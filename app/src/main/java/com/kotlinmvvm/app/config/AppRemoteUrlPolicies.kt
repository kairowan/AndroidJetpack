package com.kotlinmvvm.app.config

import com.kotlinmvvm.core.network.config.RemoteResourceUrlPolicy

/**
 * @author 浩楠
 * @date 2026/7/21 09:23
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用远程资源安全策略注册表，集中声明服务端内容可以访问的媒体 Host 边界
 */
object AppRemoteUrlPolicies {
    private val feedMediaHosts = setOf(
        "ali-img.kaiyanapp.com",
        "baobab.kaiyanapp.com",
        "eyepetizer-videos.oss-cn-beijing.aliyuncs.com",
        "img.kaiyanapp.com",
        "m.eyepetizer.net",
        "www.eyepetizer.net"
    )

    /** Feed 图片与视频只允许来自已确认支持 HTTPS 的业务域名和 CDN。 */
    val feedMedia = RemoteResourceUrlPolicy(
        allowedHosts = feedMediaHosts,
        upgradeCleartextHosts = feedMediaHosts
    )
}
