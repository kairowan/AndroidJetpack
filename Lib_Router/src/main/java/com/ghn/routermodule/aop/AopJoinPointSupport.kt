package com.ghn.routermodule.aop

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.AndroidViewModel
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint

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
 * 描述: AOP JoinPoint 辅助工具，负责解析上下文与通用返回值。
 */

object AopJoinPointSupport {

    fun actionKey(joinPoint: ProceedJoinPoint): String {
        return "${joinPoint.targetClass.name}#${joinPoint.targetMethod.name}"
    }

    fun resolveContext(target: Any?): Context? {
        return when (target) {
            is Fragment -> target.context?.applicationContext ?: target.activity?.applicationContext
            is Context -> target.applicationContext
            is AndroidViewModel -> target.getApplication<Application>().applicationContext
            is View -> target.context.applicationContext
            else -> null
        }
    }

    fun resolveContext(joinPoint: ProceedJoinPoint): Context? {
        resolveContext(joinPoint.target)?.let { return it }
        joinPoint.args?.forEach { arg ->
            resolveContext(arg)?.let { return it }
        }
        return null
    }

    fun resolveFragmentActivity(joinPoint: ProceedJoinPoint): FragmentActivity? {
        return when (val target = joinPoint.target) {
            is Fragment -> target.activity as? FragmentActivity
            is FragmentActivity -> target
            is Activity -> target as? FragmentActivity ?: target.findFragmentActivity()
            is View -> target.context.findFragmentActivity()
            else -> joinPoint.args
                ?.firstNotNullOfOrNull { arg ->
                    when (arg) {
                        is Fragment -> arg.activity as? FragmentActivity
                        is FragmentActivity -> arg
                        is Activity -> arg as? FragmentActivity ?: arg.findFragmentActivity()
                        is View -> arg.context.findFragmentActivity()
                        else -> null
                    }
                }
        }
    }

    fun resolveLifecycleOwner(joinPoint: ProceedJoinPoint): LifecycleOwner? {
        return when (val target = joinPoint.target) {
            is LifecycleOwner -> target
            else -> joinPoint.args
                ?.firstNotNullOfOrNull { arg -> arg as? LifecycleOwner }
        }
    }

    fun blockedReturnValue(joinPoint: ProceedJoinPoint, tag: String): Any? {
        val returnType = joinPoint.targetMethod.returnType
        return when (returnType) {
            Void.TYPE, java.lang.Void::class.java -> null
            Unit::class.java -> Unit
            Boolean::class.javaPrimitiveType, java.lang.Boolean::class.java -> false
            Int::class.javaPrimitiveType, java.lang.Integer::class.java -> 0
            Long::class.javaPrimitiveType, java.lang.Long::class.java -> 0L
            Float::class.javaPrimitiveType, java.lang.Float::class.java -> 0f
            Double::class.javaPrimitiveType, java.lang.Double::class.java -> 0.0
            Short::class.javaPrimitiveType, java.lang.Short::class.java -> 0.toShort()
            Byte::class.javaPrimitiveType, java.lang.Byte::class.java -> 0.toByte()
            Char::class.javaPrimitiveType, java.lang.Character::class.java -> '\u0000'
            else -> {
                AopLogger.d(
                    "[$tag] short-circuit method=${actionKey(joinPoint)} returnType=${returnType.name}, returning null"
                )
                null
            }
        }
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) {
            return current
        }
        current = current.baseContext
    }
    return null
}

private fun Activity.findFragmentActivity(): FragmentActivity? {
    return if (this is FragmentActivity) this else baseContext.findFragmentActivity()
}
