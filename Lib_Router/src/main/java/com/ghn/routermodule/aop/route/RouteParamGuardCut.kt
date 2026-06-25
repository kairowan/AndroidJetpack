package com.ghn.routermodule.aop.route

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.ghn.routermodule.aop.AopJoinPointSupport
import com.ghn.routermodule.aop.AopLogger
import com.kairowan.lib_ui_common.helper.ToastHelper
import com.therouter.router.Autowired
import java.lang.reflect.Field

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
 * 描述: RouteParamGuardCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

@AndroidAopMatchClassMethod(
    targetClassName = "com.example.basemodel.base.baseint.IBaseView",
    methodName = ["initParam", "initView", "initViewObservable", "initData"],
    type = MatchType.EXTENDS,
    overrideMethod = true,
    includeWeaving = ["com.ghn.cocknovel", "com.ghn.feature.capture", "com.ghn.module_login"]
)
class RouteParamGuardCut : MatchClassMethod {

    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        val target = joinPoint.target ?: return joinPoint.proceed()
        if (RouteParamGuardStateCenter.isBlocked(target)) {
            AopLogger.d("skip method=$methodName because route param invalid target=${joinPoint.targetClass.name}")
            return AopJoinPointSupport.blockedReturnValue(joinPoint, "RequiredRouteParam")
        }

        val invalidField = findInvalidField(target)
        if (invalidField != null) {
            RouteParamGuardStateCenter.markBlocked(target)
            val message = invalidField.message()
            AopLogger.d(
                "route param invalid key=${invalidField.routeKey()} field=${invalidField.field.name} target=${joinPoint.targetClass.name}"
            )
            if (invalidField.annotation.toastOnBlocked && message.isNotBlank()) {
                ToastHelper.showToast(message)
            }
            if (invalidField.annotation.finishIfInvalid) {
                AopJoinPointSupport.resolveFragmentActivity(joinPoint)?.finish()
            }
            return AopJoinPointSupport.blockedReturnValue(joinPoint, "RequiredRouteParam")
        }
        return joinPoint.proceed()
    }

    private fun findInvalidField(target: Any): InvalidRouteField? {
        var currentClass: Class<*>? = target.javaClass
        while (currentClass != null && currentClass != Any::class.java) {
            currentClass.declaredFields.forEach { field ->
                val annotation = field.getAnnotation(RequiredRouteParam::class.java) ?: return@forEach
                field.isAccessible = true
                val value = field.get(target)
                if (value == null) {
                    return InvalidRouteField(field, annotation, InvalidReason.MISSING)
                }
                if (annotation.notBlank && value is CharSequence && value.isBlank()) {
                    return InvalidRouteField(field, annotation, InvalidReason.BLANK)
                }
            }
            currentClass = currentClass.superclass
        }
        return null
    }
}

private data class InvalidRouteField(
    val field: Field,
    val annotation: RequiredRouteParam,
    val reason: InvalidReason
) {
    fun routeKey(): String {
        return annotation.routeKey
            .ifBlank { field.getAnnotation(Autowired::class.java)?.name.orEmpty() }
            .ifBlank { field.name }
    }

    fun message(): String {
        if (annotation.message.isNotBlank()) {
            return annotation.message
        }
        return when (reason) {
            InvalidReason.MISSING -> "页面参数 ${routeKey()} 缺失"
            InvalidReason.BLANK -> "页面参数 ${routeKey()} 不能为空"
        }
    }
}

private enum class InvalidReason {
    MISSING,
    BLANK
}
