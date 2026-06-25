package com.ghn.routermodule.aop.debug

import android.content.pm.ApplicationInfo
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
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
 * 描述: DebugOnlyCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class DebugOnlyCut : BasePointCut<DebugOnly> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: DebugOnly): Any? {
        val context = AopJoinPointSupport.resolveContext(joinPoint)
        if (context == null) {
            AopLogger.d("@DebugOnly skipped, context unavailable for ${AopJoinPointSupport.actionKey(joinPoint)}")
            return joinPoint.proceed()
        }
        if (context.isDebuggable()) {
            return joinPoint.proceed()
        }
        AopLogger.d("debug-only blocked method=${AopJoinPointSupport.actionKey(joinPoint)}")
        if (anno.toastOnBlocked && anno.message.isNotBlank()) {
            ToastHelper.showToast(anno.message)
        }
        if (anno.finishIfBlocked) {
            AopJoinPointSupport.resolveFragmentActivity(joinPoint)?.finish()
        }
        return AopJoinPointSupport.blockedReturnValue(joinPoint, "DebugOnly")
    }
}

private fun android.content.Context.isDebuggable(): Boolean {
    return applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}
