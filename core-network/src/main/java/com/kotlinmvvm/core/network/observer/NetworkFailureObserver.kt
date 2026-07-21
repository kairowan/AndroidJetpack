package com.kotlinmvvm.core.network.observer

import com.kotlinmvvm.core.network.result.NetworkFailure

/**
 * @author 浩楠
 * @date 2026/7/21 13:35
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 网络失败诊断观察契约，允许宿主统一接入日志、指标或异常平台而不侵入请求结果
 */
fun interface NetworkFailureObserver {
    /** 接收稳定失败类型和可选原始原因；实现不得记录令牌、请求正文或向外抛出异常。 */
    fun onFailure(failure: NetworkFailure, cause: Throwable?)

    companion object {
        val None = NetworkFailureObserver { _, _ -> }
    }
}
