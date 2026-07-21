package com.kotlinmvvm.core.network.interceptor

import okhttp3.Request

/**
 * @author 浩楠
 * @date 2026/7/20 10:38
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 按请求提供动态公共请求头，可在业务工程中接入登录令牌、租户或渠道信息
 */
fun interface NetworkHeaderProvider {
    /** 根据当前请求动态提供请求头，可按 Host 区分不同 Base URL 的认证信息。 */
    fun headers(request: Request): Map<String, String>

    companion object {
        val Empty = NetworkHeaderProvider { _ -> emptyMap() }
    }
}
