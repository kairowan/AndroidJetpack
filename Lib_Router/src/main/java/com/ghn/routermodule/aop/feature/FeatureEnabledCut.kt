package com.ghn.routermodule.aop.feature

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.ghn.routermodule.aop.AopJoinPointSupport
import com.ghn.routermodule.aop.AopLogger
import com.ghn.routermodule.feature.FeatureFlagCenter
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
 * 描述: FeatureEnabledCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class FeatureEnabledCut : BasePointCut<FeatureEnabled> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: FeatureEnabled): Any? {
        if (FeatureFlagCenter.isEnabled(anno.featureKey, anno.defaultEnabled)) {
            return joinPoint.proceed()
        }
        AopLogger.d(
            "feature blocked key=${anno.featureKey} method=${AopJoinPointSupport.actionKey(joinPoint)}"
        )
        if (anno.toastOnBlocked && anno.message.isNotBlank()) {
            ToastHelper.showToast(anno.message)
        }
        if (anno.finishIfBlocked) {
            AopJoinPointSupport.resolveFragmentActivity(joinPoint)?.finish()
        }
        return AopJoinPointSupport.blockedReturnValue(joinPoint, "FeatureEnabled")
    }
}
