package com.kotlinmvvm.app.window

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kotlinmvvm.core.player.model.VideoWindowMode

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Activity 视频窗口宿主控制器，执行页面请求并在离开播放流时恢复原始窗口配置
 */
class ActivityVideoWindowController(
    private val activity: Activity
) {
    private val originalOrientation = activity.requestedOrientation
    private val originalSystemBarsVisible = ViewCompat.getRootWindowInsets(activity.window.decorView)
        ?.isVisible(WindowInsetsCompat.Type.systemBars())
        ?: true

    /** 应用 Feature 请求的窗口模式；大屏设备可能由系统忽略方向请求，布局仍需自适应。 */
    fun apply(mode: VideoWindowMode) {
        if (mode == VideoWindowMode.INLINE && activity.isChangingConfigurations) return
        val orientation = when (mode) {
            VideoWindowMode.INLINE -> originalOrientation
            VideoWindowMode.PORTRAIT_FULLSCREEN -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            VideoWindowMode.LANDSCAPE_FULLSCREEN -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        if (activity.requestedOrientation != orientation) activity.requestedOrientation = orientation

        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        if (mode == VideoWindowMode.INLINE) {
            restoreSystemBars(controller)
        } else {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    /** 恢复控制器创建时记录的方向和系统栏可见性。 */
    fun restore() {
        if (activity.requestedOrientation != originalOrientation) {
            activity.requestedOrientation = originalOrientation
        }
        restoreSystemBars(WindowCompat.getInsetsController(activity.window, activity.window.decorView))
    }

    private fun restoreSystemBars(controller: WindowInsetsControllerCompat) {
        if (originalSystemBarsVisible) {
            controller.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
