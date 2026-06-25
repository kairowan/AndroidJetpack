package com.ghn.cocknovel.ui.activity

import android.graphics.Color
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.Choreographer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.basemodel.base.BaseApplication
import com.ghn.cocknovel.App
import com.ghn.cocknovel.R
import com.ghn.cocknovel.startup.AppStartupPlan

/**
 * 轻量启动图页。
 *
 * 先尽快把整张启动图显示出来，再在图片可见期间完成应用核心初始化，
 * 避免用户长时间停留在 Android 12+ 系统启动页的纯色背景上。
 */
class SplashActivity : AppCompatActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var hasStartedInitialization = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen()
        }
        super.onCreate(savedInstanceState)
        configureFullscreenImageWindow()
        setContentView(R.layout.activity_splash)
        scheduleStartupAfterFirstFrame()
    }

    private fun configureFullscreenImageWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isAppearanceLightNavigationBars = false
            }
        }
    }

    private fun scheduleStartupAfterFirstFrame() {
        Choreographer.getInstance().postFrameCallback {
            mainHandler.post {
                startAppInitializationIfNeeded()
            }
        }
    }

    private fun startAppInitializationIfNeeded() {
        if (hasStartedInitialization) {
            return
        }
        hasStartedInitialization = true
        (application as BaseApplication).ensureBaseRuntimeInitialized()
        AppStartupPlan.start(application as App)
        navigateToStart()
    }

    private fun navigateToStart() {
        startActivity(Intent(this, StartActivity::class.java))
        overridePendingTransition(0, 0)
        finish()
    }
}
