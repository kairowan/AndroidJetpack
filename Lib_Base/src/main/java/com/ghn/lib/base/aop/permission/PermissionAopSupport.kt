package com.ghn.lib.base.aop.permission

import android.app.Activity
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.ghn.routermodule.aop.AopLogger
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.PermissionNames
import com.hjq.permissions.permission.base.IPermission
import com.hjq.permissions.permission.dangerous.StandardDangerousPermission
import com.kairowan.lib_ui_common.helper.ToastHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
 * 描述: PermissionAopSupport 辅助工具，负责封装对应场景下的通用处理逻辑。
 */

internal data class PermissionRequestConfig(
    val tag: String,
    val deniedMessage: String,
    val deniedForeverMessage: String,
    val partialGrantedMessage: String,
    val openSettingsOnDeniedForever: Boolean
)

internal object PermissionAopSupport {

    fun handle(
        joinPoint: ProceedJoinPoint,
        config: PermissionRequestConfig,
        permissionNames: Array<out String>
    ): Any? {
        // 普通函数场景先发起权限申请，真正的 proceed 延迟到回调成功后再执行。
        requestPermission(joinPoint, config, permissionNames) { granted ->
            if (!granted || shouldSkipProceed(joinPoint.target)) {
                return@requestPermission
            }
            joinPoint.proceed()
        }
        return null
    }

    suspend fun handleSuspend(
        joinPoint: ProceedJoinPointSuspend,
        config: PermissionRequestConfig,
        permissionNames: Array<out String>
    ) {
        // suspend 切面要显式切回主线程等待权限结果，避免在后台线程弹权限请求。
        val granted = withContext(Dispatchers.Main.immediate) {
            suspendCoroutine { continuation ->
                requestPermission(joinPoint, config, permissionNames) { continuation.resume(it) }
            }
        }
        if (!granted || shouldSkipProceed(joinPoint.target)) {
            joinPoint.proceedIgnoreOther { null }
            return
        }
        joinPoint.proceed()
    }

    private fun requestPermission(
        joinPoint: ProceedJoinPoint,
        config: PermissionRequestConfig,
        permissionNames: Array<out String>,
        onResult: (Boolean) -> Unit
    ) {
        val permissions = permissionNames.map(::toPermission)
        AopLogger.d("permission tag=${config.tag} permissions=${permissionNames.joinToString()}")
        when (val target = resolvePermissionTarget(joinPoint)) {
            is Fragment -> XXPermissions.with(target)
                .permissions(permissions)
                .request(createPermissionCallback(target, config, onResult))

            is Activity -> XXPermissions.with(target)
                .permissions(permissions)
                .request(createPermissionCallback(target, config, onResult))

            else -> {
                AopLogger.d("unsupported permission target=${joinPoint.targetClass.name}")
                ToastHelper.showToast("权限申请请放在 Activity 或 Fragment 中使用")
                onResult(false)
            }
        }
    }

    private fun createPermissionCallback(
        target: Any,
        config: PermissionRequestConfig,
        onResult: (Boolean) -> Unit
    ) = object : OnPermissionCallback {
        override fun onPermissionResult(
            grantedPermissions: List<IPermission>,
            deniedPermissions: List<IPermission>
        ) {
            if (deniedPermissions.isEmpty()) {
                onResult(true)
                return
            }
            // 统一在这里收口“永久拒绝 / 部分授权 / 普通拒绝”三类结果，避免每个切面重复处理。
            val doNotAskAgain = isDoNotAskAgain(target, deniedPermissions)
            AopLogger.d("permission denied tag=${config.tag} doNotAskAgain=$doNotAskAgain")
            if (doNotAskAgain) {
                ToastHelper.showToast(config.deniedForeverMessage)
                if (config.openSettingsOnDeniedForever) {
                    openPermissionSettings(target, deniedPermissions)
                }
            } else if (grantedPermissions.isNotEmpty()) {
                AopLogger.d("partial permission granted tag=${config.tag}")
                ToastHelper.showToast(config.partialGrantedMessage)
            } else {
                ToastHelper.showToast(config.deniedMessage)
            }
            onResult(false)
        }
    }

    private fun openPermissionSettings(target: Any, permissions: List<IPermission>) {
        when (target) {
            is Fragment -> target.activity?.let { XXPermissions.startPermissionActivity(it, permissions) }
            is Activity -> XXPermissions.startPermissionActivity(target, permissions)
        }
    }

    private fun isDoNotAskAgain(target: Any, deniedPermissions: List<IPermission>): Boolean {
        return when (target) {
            is Fragment -> target.activity?.let {
                XXPermissions.isDoNotAskAgainPermissions(it, deniedPermissions)
            } ?: false

            is Activity -> XXPermissions.isDoNotAskAgainPermissions(target, deniedPermissions)
            else -> false
        }
    }

    private fun toPermission(permissionName: String): IPermission {
        return when (permissionName) {
            PermissionNames.CAMERA -> PermissionLists.getCameraPermission()
            PermissionNames.RECORD_AUDIO -> PermissionLists.getRecordAudioPermission()
            PermissionNames.ACCESS_FINE_LOCATION -> PermissionLists.getAccessFineLocationPermission()
            PermissionNames.ACCESS_COARSE_LOCATION -> PermissionLists.getAccessCoarseLocationPermission()
            PermissionNames.ACCESS_BACKGROUND_LOCATION -> PermissionLists.getAccessBackgroundLocationPermission()
            PermissionNames.READ_MEDIA_IMAGES -> PermissionLists.getReadMediaImagesPermission()
            PermissionNames.READ_MEDIA_VIDEO -> PermissionLists.getReadMediaVideoPermission()
            PermissionNames.READ_MEDIA_AUDIO -> PermissionLists.getReadMediaAudioPermission()
            PermissionNames.READ_EXTERNAL_STORAGE -> PermissionLists.getReadExternalStoragePermission()
            PermissionNames.WRITE_EXTERNAL_STORAGE -> PermissionLists.getWriteExternalStoragePermission()
            PermissionNames.POST_NOTIFICATIONS -> PermissionLists.getPostNotificationsPermission()
            else -> StandardDangerousPermission(permissionName, Build.VERSION_CODES.M)
        }
    }

    private fun shouldSkipProceed(target: Any?): Boolean {
        if (target !is LifecycleOwner) {
            return false
        }
        return target.lifecycle.currentState == Lifecycle.State.DESTROYED
    }

    private fun resolvePermissionTarget(joinPoint: ProceedJoinPoint): Any? {
        return when (val target = joinPoint.target) {
            is Fragment, is Activity -> target
            else -> joinPoint.args?.firstOrNull { it is Fragment || it is Activity }
        }
    }
}
