package com.ghn.lib.base.aop.confirm

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
 * 描述: ConfirmAction 动作模型，负责描述单次可执行的业务行为。
 */

@AndroidAopPointCut(ConfirmActionCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class ConfirmAction(
    val title: String = "操作确认",
    val message: String,
    val confirmText: String = "确定",
    val cancelText: String = "取消",
    val cancelable: Boolean = true
)
