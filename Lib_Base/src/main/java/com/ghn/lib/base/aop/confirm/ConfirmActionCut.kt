package com.ghn.lib.base.aop.confirm

import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.ghn.routermodule.aop.AopJoinPointSupport
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
 * 描述: ConfirmActionCut 切面实现，负责统一拦截并编排相关横切逻辑。
 */

class ConfirmActionCut : BasePointCut<ConfirmAction> {

    override fun invoke(joinPoint: ProceedJoinPoint, anno: ConfirmAction): Any? {
        if (!supportsAsyncConfirm(joinPoint)) {
            AopLogger.d(
                "@ConfirmAction only supports Unit/void, method=${AopJoinPointSupport.actionKey(joinPoint)}"
            )
            return joinPoint.proceed()
        }
        val activity = AopJoinPointSupport.resolveFragmentActivity(joinPoint)
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            AopLogger.d("@ConfirmAction skipped, activity unavailable for ${AopJoinPointSupport.actionKey(joinPoint)}")
            return joinPoint.proceed()
        }
        val showDialog: () -> Unit = {
            AlertDialog.Builder(activity)
                .setTitle(anno.title)
                .setMessage(anno.message)
                .setPositiveButton(anno.confirmText) { dialog, _ ->
                    dialog.dismiss()
                    if (shouldSkipProceed(joinPoint)) {
                        return@setPositiveButton
                    }
                    joinPoint.proceed()
                }
                .setNegativeButton(anno.cancelText) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(anno.cancelable)
                .show()
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showDialog()
        } else {
            activity.runOnUiThread(showDialog)
        }
        return Unit
    }

    private fun supportsAsyncConfirm(joinPoint: ProceedJoinPoint): Boolean {
        val returnType = joinPoint.targetMethod.returnType
        return returnType == Void.TYPE || returnType == Unit::class.java
    }

    private fun shouldSkipProceed(joinPoint: ProceedJoinPoint): Boolean {
        val lifecycleOwner = AopJoinPointSupport.resolveLifecycleOwner(joinPoint) ?: return false
        return lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED
    }
}
