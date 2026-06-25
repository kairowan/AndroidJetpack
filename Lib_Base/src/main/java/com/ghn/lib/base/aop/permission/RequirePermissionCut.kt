package com.ghn.lib.base.aop.permission

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend

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
 * 描述: RequirePermissionCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class RequirePermissionCut : BasePointCutSuspend<RequirePermission> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: RequirePermission): Any? {
        return PermissionAopSupport.handle(joinPoint, anno.toConfig(), anno.value)
    }

    override suspend fun invokeSuspend(
        joinPoint: ProceedJoinPointSuspend,
        anno: RequirePermission
    ) {
        PermissionAopSupport.handleSuspend(joinPoint, anno.toConfig(), anno.value)
    }
}

private fun RequirePermission.toConfig(): PermissionRequestConfig {
    return PermissionRequestConfig(
        tag = tag,
        deniedMessage = deniedMessage,
        deniedForeverMessage = deniedForeverMessage,
        partialGrantedMessage = partialGrantedMessage,
        openSettingsOnDeniedForever = openSettingsOnDeniedForever
    )
}
