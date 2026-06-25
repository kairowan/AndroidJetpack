package com.ghn.cocknovel.ui.navigation

import android.content.Context
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.UserPageRouter
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
 * 描述: 用户页导航提供者，负责对接用户页相关路由能力。
 */

@Singleton
@ServiceProvider(returnType = UserPageRouter::class)
class UserPageNavigatorProvider : UserPageRouter {
    override fun openUserKey(context: Context?) {
        RouterPath.User.UserKEY.navigate(context)
    }
}
