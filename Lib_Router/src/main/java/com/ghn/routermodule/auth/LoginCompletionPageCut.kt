package com.ghn.routermodule.auth

import android.app.Activity
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
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
 * 描述: LoginCompletionPageCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

@AndroidAopMatchClassMethod(
    targetClassName = "androidx.appcompat.app.AppCompatActivity",
    methodName = ["onDestroy"],
    type = MatchType.EXTENDS,
    overrideMethod = true,
    includeWeaving = ["com.ghn.cocknovel", "com.ghn.feature.capture", "com.ghn.module_login"]
)
class LoginCompletionPageCut : MatchClassMethod {

    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        val page = joinPoint.targetClass.getAnnotation(LoginCompletionPage::class.java)
            ?: return joinPoint.proceed()

        val activity = joinPoint.target as? Activity ?: return joinPoint.proceed()
        val loginRequestId = (activity as? LoginRequestSessionOwner)?.loginRequestSessionId
        val didCompleteLogin = LoginCompletionStateCenter.consumeCompleted(activity)
        if (activity.isFinishing && !activity.isChangingConfigurations) {
            if (didCompleteLogin) {
                AopLogger.d("login page finished after explicit success target=${joinPoint.targetClass.name}")
            } else if (
                page.clearPendingActionOnCancel &&
                LoginRequiredActionCenter.hasPendingAction(loginRequestId)
            ) {
                AopLogger.d("login page canceled, clear pending actions target=${joinPoint.targetClass.name}")
                LoginRequiredActionCenter.clearPendingAction(loginRequestId)
            }
        }
        return joinPoint.proceed()
    }
}
