package com.ghn.routermodule.aop.page

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.ghn.routermodule.aop.AopJoinPointSupport
import com.ghn.routermodule.aop.AopLogger
import com.kairowan.lib_ui_common.helper.ToastHelper

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
 * 描述: PageAccessGuardCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

@AndroidAopMatchClassMethod(
    targetClassName = "com.example.basemodel.base.baseint.IBaseView",
    methodName = ["initParam", "initView", "initViewObservable", "initData"],
    type = MatchType.EXTENDS,
    overrideMethod = true,
    includeWeaving = ["com.ghn.cocknovel", "com.ghn.feature.capture", "com.ghn.module_login"]
)
class PageAccessGuardCut : MatchClassMethod {

    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        val target = joinPoint.target ?: return joinPoint.proceed()
        if (PageAccessGuardStateCenter.isBlocked(target)) {
            AopLogger.d(
                "skip method=$methodName because page access already blocked target=${joinPoint.targetClass.name}"
            )
            return AopJoinPointSupport.blockedReturnValue(joinPoint, "PageAccessGuard")
        }

        val guard = joinPoint.targetClass.getAnnotation(PageAccessGuard::class.java)
            ?: return joinPoint.proceed()

        val block = PageAccessGuardSupport.resolveBlock(
            guard = guard,
            context = AopJoinPointSupport.resolveContext(joinPoint)
        ) ?: return joinPoint.proceed()

        PageAccessGuardStateCenter.markBlocked(target)
        val actionKey = AopJoinPointSupport.actionKey(joinPoint)
        AopLogger.d("page access blocked method=$actionKey")
        if (block.toastOnBlocked && block.message.isNotBlank()) {
            ToastHelper.showToast(block.message)
        }
        if (guard.finishIfBlocked) {
            AopJoinPointSupport.resolveFragmentActivity(joinPoint)?.finish()
        }
        return AopJoinPointSupport.blockedReturnValue(joinPoint, "PageAccessGuard")
    }
}
