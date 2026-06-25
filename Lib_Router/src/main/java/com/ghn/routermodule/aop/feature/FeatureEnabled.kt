package com.ghn.routermodule.aop.feature

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
 * 描述: FeatureEnabled 类型定义，负责承载对应模块中的基础能力或数据结构。
 */

@AndroidAopPointCut(FeatureEnabledCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class FeatureEnabled(
    val featureKey: String,
    val defaultEnabled: Boolean = true,
    val message: String = "当前功能暂未开放",
    val toastOnBlocked: Boolean = true,
    val finishIfBlocked: Boolean = false
)
