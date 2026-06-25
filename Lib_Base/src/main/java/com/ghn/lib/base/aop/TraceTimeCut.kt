package com.ghn.lib.base.aop

import android.os.SystemClock
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.ghn.routermodule.aop.AopLogger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

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
 * 描述: TraceTimeCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class TraceTimeCut : BasePointCutSuspend<TraceTime> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: TraceTime): Any? {
        val start = SystemClock.elapsedRealtime()
        return try {
            joinPoint.proceed()
        } finally {
            logCost(joinPoint.targetClass.name, joinPoint.targetMethod.name, anno, start)
        }
    }

    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: TraceTime) {
        val start = SystemClock.elapsedRealtime()
        // AndroidAOP 的 suspend 切面建议显式包一层 withContext，否则 continuation
        // 恢复阶段可能触发内部状态异常。这里仅做计时，不需要走返回值监听链路。
        withContext(currentCoroutineContext() + CoroutineName("TraceTimeCut")) {
            try {
                joinPoint.proceed()
            } finally {
                logCost(joinPoint.targetClass.name, joinPoint.targetMethod.name, anno, start)
            }
        }

    }

    private fun logCost(className: String, methodName: String, anno: TraceTime, start: Long) {
        val cost = SystemClock.elapsedRealtime() - start
        val label = anno.value.ifBlank { "$className#$methodName" }
        val level = if (cost >= anno.warnAtMillis) "WARN" else "INFO"
        AopLogger.d("[$level] $label cost=${cost}ms")
    }
}
