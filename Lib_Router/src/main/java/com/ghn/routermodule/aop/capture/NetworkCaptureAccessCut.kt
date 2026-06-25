package com.ghn.routermodule.aop.capture

import android.content.Context
import android.content.pm.ApplicationInfo
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.ghn.routermodule.aop.AopJoinPointSupport
import com.ghn.routermodule.aop.AopLogger
import com.ghn.routermodule.feature.FeatureFlagCenter
import com.ghn.routermodule.feature.FeatureKeys
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
 * 描述: NetworkCaptureAccessCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class NetworkCaptureAccessCut : BasePointCut<NetworkCaptureAccess> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: NetworkCaptureAccess): Any? {
        val context = AopJoinPointSupport.resolveContext(joinPoint)
        if (context == null) {
            AopLogger.d(
                "@NetworkCaptureAccess skipped, context unavailable for ${AopJoinPointSupport.actionKey(joinPoint)}"
            )
            return joinPoint.proceed()
        }
        if (!context.isDebuggable()) {
            AopLogger.d("network-capture blocked by debug check method=${AopJoinPointSupport.actionKey(joinPoint)}")
            return block(joinPoint, anno.debugBlockedMessage, anno)
        }
        if (!FeatureFlagCenter.isEnabled(FeatureKeys.NETWORK_CAPTURE)) {
            AopLogger.d("network-capture blocked by feature flag method=${AopJoinPointSupport.actionKey(joinPoint)}")
            return block(joinPoint, anno.featureBlockedMessage, anno)
        }
        return joinPoint.proceed()
    }

    private fun block(
        joinPoint: ProceedJoinPoint,
        message: String,
        anno: NetworkCaptureAccess
    ): Any? {
        if (anno.toastOnBlocked && message.isNotBlank()) {
            ToastHelper.showToast(message)
        }
        if (anno.finishIfBlocked) {
            AopJoinPointSupport.resolveFragmentActivity(joinPoint)?.finish()
        }
        return AopJoinPointSupport.blockedReturnValue(joinPoint, "NetworkCaptureAccess")
    }
}

private fun Context.isDebuggable(): Boolean {
    return applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}
