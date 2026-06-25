package com.ghn.cocknovel.ui.navigation

import android.content.Context
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.SettingPageRouter
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
 * 描述: 设置页导航提供者，负责对接设置页相关路由能力。
 */

@Singleton
@ServiceProvider(returnType = SettingPageRouter::class)
class SettingPageNavigatorProvider : SettingPageRouter {
    override fun openSettings(context: Context?) {
        RouterPath.Setting.SETTINGS.navigate(context)
    }

    override fun openFontSettings(context: Context?) {
        RouterPath.Setting.FONT.navigate(context)
    }
}
