package com.ghn.lib.base.aop.permission.capability

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.ghn.lib.base.aop.permission.PermissionAopSupport
import com.ghn.lib.base.aop.permission.PermissionRequestConfig
import com.hjq.permissions.permission.PermissionNames

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
 * 描述: RequireCameraPermissionCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class RequireCameraPermissionCut : BasePointCutSuspend<RequireCameraPermission> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: RequireCameraPermission): Any? {
        return PermissionAopSupport.handle(joinPoint, anno.toConfig(), CAMERA_PERMISSION)
    }

    override suspend fun invokeSuspend(
        joinPoint: ProceedJoinPointSuspend,
        anno: RequireCameraPermission
    ) {
        PermissionAopSupport.handleSuspend(joinPoint, anno.toConfig(), CAMERA_PERMISSION)
    }
}

private val CAMERA_PERMISSION = arrayOf(PermissionNames.CAMERA)

private fun RequireCameraPermission.toConfig(): PermissionRequestConfig {
    return PermissionRequestConfig(
        tag = tag,
        deniedMessage = deniedMessage,
        deniedForeverMessage = deniedForeverMessage,
        partialGrantedMessage = partialGrantedMessage,
        openSettingsOnDeniedForever = openSettingsOnDeniedForever
    )
}
