package com.ghn.cocknovel.ui.web

import com.ghn.routermodule.CoreBridgeWebRouter
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
 * 描述: 核心桥接页面导航提供者，负责封装桥接 Web 页跳转能力。
 */

@Singleton
@ServiceProvider(returnType = CoreBridgeWebRouter::class)
class CoreBridgeWebNavigatorProvider : CoreBridgeWebRouter
