package com.ghn.lib.base.aop.permission

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut

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
 * 描述: RequirePermission 类型定义，负责承载对应模块中的基础能力或数据结构。
 */

@AndroidAopPointCut(RequirePermissionCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER)
annotation class RequirePermission(
    vararg val value: String,
    val tag: String = "",
    val deniedMessage: String = "获取权限失败",
    val deniedForeverMessage: String = "权限被永久拒绝，请手动开启",
    val partialGrantedMessage: String = "已授予部分权限，请继续授权剩余权限",
    val openSettingsOnDeniedForever: Boolean = true
)
