package com.ghn.cocknovel.ui.web

import android.content.Context
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.WebPageRequest
import com.ghn.routermodule.WebPageRouter
import com.ghn.routermodule.WebPageRouteContract
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
 * 描述: 通用 Web 页面导航提供者，负责对接页面路由实现。
 */

@Singleton
@ServiceProvider(returnType = WebPageRouter::class)
class WebPageNavigatorProvider : WebPageRouter {
    override fun open(request: WebPageRequest, context: Context?) {
        RouterPath.Web.WEBVIEW.navigate(context) {
            WebPageRouteContract.applyTo(this, request)
        }
    }
}
