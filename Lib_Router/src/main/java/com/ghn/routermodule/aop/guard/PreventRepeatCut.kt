package com.ghn.routermodule.aop.guard

import android.os.SystemClock
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.ghn.routermodule.aop.AopJoinPointSupport
import com.ghn.routermodule.aop.AopLogger
import com.kairowan.lib_ui_common.helper.ToastHelper
import java.util.concurrent.ConcurrentHashMap

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
 * 描述: PreventRepeatCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class PreventRepeatCut : BasePointCut<PreventRepeat> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: PreventRepeat): Any? {
        if (anno.intervalMillis <= 0L) {
            return joinPoint.proceed()
        }
        val key = anno.key.ifBlank { AopJoinPointSupport.actionKey(joinPoint) }
        // 同一个 key 在时间窗内只允许通过一次，适合保护点击、Bridge 入口等高频触发点。
        if (PreventRepeatGate.tryPass(key, anno.intervalMillis)) {
            return joinPoint.proceed()
        }
        AopLogger.d("repeat blocked key=$key interval=${anno.intervalMillis}")
        if (anno.toastOnBlocked && anno.message.isNotBlank()) {
            ToastHelper.showToast(anno.message)
        }
        return AopJoinPointSupport.blockedReturnValue(joinPoint, "PreventRepeat")
    }
}

private object PreventRepeatGate {
    private val lastInvokeTime = ConcurrentHashMap<String, Long>()

    fun tryPass(key: String, intervalMillis: Long): Boolean {
        val now = SystemClock.elapsedRealtime()
        while (true) {
            val lastTime = lastInvokeTime[key]
            if (lastTime != null && now - lastTime < intervalMillis) {
                return false
            }
            if (lastTime == null) {
                if (lastInvokeTime.putIfAbsent(key, now) == null) {
                    return true
                }
            } else if (lastInvokeTime.replace(key, lastTime, now)) {
                return true
            }
        }
    }
}
