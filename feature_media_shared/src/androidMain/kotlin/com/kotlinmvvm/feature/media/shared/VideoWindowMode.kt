package com.kotlinmvvm.feature.media.shared

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 从 Compose 宿主 Context 中查找 Activity；非 Activity 宿主返回 null。
 */
tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

/**
 * 将共享全屏状态映射为 Android 屏幕方向和系统栏行为。
 */
fun Activity.applyVideoWindowMode(mode: FullscreenMode) {
    val targetOrientation = when (mode) {
        FullscreenMode.NONE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        FullscreenMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        FullscreenMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
    if (requestedOrientation != targetOrientation) {
        requestedOrientation = targetOrientation
    }

    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    if (mode == FullscreenMode.NONE) {
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    } else {
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}
