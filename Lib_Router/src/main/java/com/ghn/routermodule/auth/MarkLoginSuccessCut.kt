package com.ghn.routermodule.auth

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.ghn.routermodule.aop.AopLogger

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
 * 描述: MarkLoginSuccessCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class MarkLoginSuccessCut : BasePointCut<MarkLoginSuccess> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: MarkLoginSuccess): Any? {
        val result = joinPoint.proceed()
        val target = joinPoint.target
        if (target != null) {
            LoginCompletionStateCenter.markCompleted(target)
            AopLogger.d("mark login success target=${joinPoint.targetClass.name}")
        } else {
            AopLogger.d("skip mark login success because target is null")
        }
        val loginRequestId = (target as? LoginRequestSessionOwner)?.loginRequestSessionId
        LoginRequiredActionCenter.onLoginSuccess(loginRequestId)
        return result
    }
}
