package com.ghn.lib.base.aop.network

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
 * 描述: RequireNetworkCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class RequireNetworkCut : BasePointCut<RequireNetwork> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: RequireNetwork): Any? {
        val context = AopJoinPointSupport.resolveContext(joinPoint)
        if (context == null) {
            AopLogger.d("@RequireNetwork skipped, unsupported target=${joinPoint.targetClass.name}")
            return joinPoint.proceed()
        }
        if (NetworkAvailability.isAvailable(context)) {
            return joinPoint.proceed()
        }
        AopLogger.d("network unavailable method=${AopJoinPointSupport.actionKey(joinPoint)}")
        if (anno.toastOnUnavailable && anno.message.isNotBlank()) {
            ToastHelper.showToast(anno.message)
        }
        return AopJoinPointSupport.blockedReturnValue(joinPoint, "RequireNetwork")
    }
}
