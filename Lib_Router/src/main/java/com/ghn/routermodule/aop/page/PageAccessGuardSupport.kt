package com.ghn.routermodule.aop.page

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.fragment.app.FragmentActivity
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
 * 描述: PageAccessGuardSupport 辅助工具，负责封装对应场景下的通用处理逻辑。
 */

internal data class PageAccessBlock(
    val message: String,
    val toastOnBlocked: Boolean
)

object PageAccessGuardSupport {

    internal fun resolveBlock(
        guard: PageAccessGuard,
        context: Context?
    ): PageAccessBlock? {
        if (guard.debugOnly && !isDebuggable(context)) {
            return PageAccessBlock(
                message = guard.debugBlockedMessage,
                toastOnBlocked = guard.debugToastOnBlocked
            )
        }
        if (guard.featureKey.isNotBlank() &&
            !FeatureFlagCenter.isEnabled(guard.featureKey, guard.featureDefaultEnabled)
        ) {
            return PageAccessBlock(
                message = guard.featureBlockedMessage,
                toastOnBlocked = guard.featureToastOnBlocked
            )
        }
        return null
    }

    fun enforce(activity: FragmentActivity): Boolean {
        val guard = activity.javaClass.getAnnotation(PageAccessGuard::class.java) ?: return false
        val block = resolveBlock(guard, activity.applicationContext) ?: return false
        if (block.toastOnBlocked && block.message.isNotBlank()) {
            ToastHelper.showToast(block.message)
        }
        if (guard.finishIfBlocked) {
            activity.finish()
        }
        return true
    }

    private fun isDebuggable(context: Context?): Boolean {
        if (context == null) {
            return true
        }
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
}
