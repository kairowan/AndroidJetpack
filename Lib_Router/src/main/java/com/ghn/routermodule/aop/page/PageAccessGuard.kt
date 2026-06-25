package com.ghn.routermodule.aop.page

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
 * 描述: PageAccessGuard 类型定义，负责承载对应模块中的基础能力或数据结构。
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PageAccessGuard(
    val debugOnly: Boolean = false,
    val debugBlockedMessage: String = "当前环境不可用",
    val debugToastOnBlocked: Boolean = true,
    val featureKey: String = "",
    val featureDefaultEnabled: Boolean = true,
    val featureBlockedMessage: String = "当前功能暂未开放",
    val featureToastOnBlocked: Boolean = true,
    val finishIfBlocked: Boolean = true
)
