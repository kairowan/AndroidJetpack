package com.ghn.feature.capture

import android.content.Context
import com.ghn.routermodule.NetworkCaptureRouter
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.navigate
import com.therouter.inject.ServiceProvider
import com.therouter.inject.Singleton

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
 * 描述: 抓包页面导航提供者，负责对接网络抓包页跳转能力。
 */
@Singleton
@ServiceProvider(returnType = NetworkCaptureRouter::class)
class NetworkCaptureNavigatorProvider : NetworkCaptureRouter {
    override fun openNetworkCapture(context: Context?) {
        RouterPath.Net.NETWORKCAPTURE.navigate(context)
    }
}
