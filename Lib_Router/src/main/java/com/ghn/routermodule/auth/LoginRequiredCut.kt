package com.ghn.routermodule.auth

import androidx.lifecycle.Lifecycle
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.ghn.routermodule.aop.AopJoinPointSupport
import com.ghn.routermodule.aop.AopLogger
import com.ghn.routermodule.LoginRouter
import com.ghn.routermodule.routerServiceOrNull
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
 * 描述: LoginRequiredCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class LoginRequiredCut : BasePointCut<LoginRequired> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: LoginRequired): Any? {
        if (LoginSession.isLoggedIn()) {
            return joinPoint.proceed()
        }

        val actionKey = AopJoinPointSupport.actionKey(joinPoint)
        ToastHelper.showToast(anno.message)
        val canResume = anno.resumeAfterLogin && canResumeAfterLogin(joinPoint)
        // 需要登录但允许登录后续执行的场景，会把原始动作暂存到 ActionCenter 中等待恢复。
        val enqueueResult = if (canResume) {
            LoginRequiredActionCenter.enqueue(actionKey) {
                if (shouldSkipResume(joinPoint)) {
                    AopLogger.d("skip login resume for destroyed target=$actionKey")
                    return@enqueue
                }
                joinPoint.proceed()
            }
        } else {
            null
        }
        if (anno.openLoginPage &&
            ((enqueueResult?.shouldOpenLogin == true) || !anno.resumeAfterLogin || !canResume)
        ) {
            openLoginPage(enqueueResult?.sessionId)
        }
        return AopJoinPointSupport.blockedReturnValue(joinPoint, "LoginRequired")
    }

    private fun canResumeAfterLogin(joinPoint: ProceedJoinPoint): Boolean {
        val returnType = joinPoint.targetMethod.returnType
        val canResume = returnType == Void.TYPE || returnType == Unit::class.java
        if (!canResume) {
            AopLogger.d(
                "@LoginRequired only supports Unit/void return for resume, method=${AopJoinPointSupport.actionKey(joinPoint)} returnType=${returnType.name}"
            )
        }
        return canResume
    }

    private fun shouldSkipResume(joinPoint: ProceedJoinPoint): Boolean {
        val lifecycleOwner = AopJoinPointSupport.resolveLifecycleOwner(joinPoint) ?: return false
        return lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED
    }

    private fun openLoginPage(loginRequestId: String?) {
        val loginRouter = routerServiceOrNull<LoginRouter>()
        if (loginRouter == null) {
            AopLogger.d("LoginRouter provider missing")
            return
        }
        loginRouter.openLogin(loginRequestId)
    }
}
