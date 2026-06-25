package com.ghn.lib.base.aop

import android.view.View
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
 * 描述: GlobalClickTraceCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"],
    type = MatchType.EXTENDS,
    includeWeaving = ["com.ghn.cocknovel", "com.ghn.feature.capture", "com.ghn.module_login"]
)
class GlobalClickTraceCut : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        val target = joinPoint.targetClass.name
        val view = joinPoint.args?.firstOrNull { it is View } as? View
        val viewName = view?.let(::resolveViewName) ?: "unknown_view"
        AopLogger.d("click target=$target method=$methodName view=$viewName")
        return joinPoint.proceed()
    }

    private fun resolveViewName(view: View): String {
        if (view.id == View.NO_ID) return "no_id"
        return try {
            view.resources.getResourceEntryName(view.id)
        } catch (_: Exception) {
            view.id.toString()
        }
    }
}
