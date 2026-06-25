package com.ghn.cocknovel.ui.activity

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.basemodel.base.baseact.BaseActivity
import com.ghn.cocknovel.databinding.ActivityStartBinding
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.ghn.routermodule.aop.guard.PreventRepeat
import com.kt.network.utils.RandomverificationCode

class StartActivity : BaseActivity<ActivityStartBinding, BookStoreViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        configureFullscreenImageWindow()
        super.onCreate(savedInstanceState)
    }

    override fun initContentView(savedInstanceState: Bundle?): ActivityStartBinding =
        ActivityStartBinding.inflate(layoutInflater)


    override fun initParam() {


    }

    override fun initView() {
        // 验证码生成不是首帧必需，延后到布局挂载后再做，避免继续阻塞启动首屏。
        mBinding.ivCode.post {
            updateVerifyCode()
        }
        mBinding.ivCode.setOnClickListener {
            updateVerifyCode()
        }
        mBinding.btSignIn.setOnClickListener(::onSignInClick)
    }

    override fun initViewObservable() {

    }

    override fun initData() {
        // 设备形态日志不参与首屏渲染，延后到首帧后执行即可。
        mBinding.root.post {
            isDeviceFolded(this)
        }
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

    @PreventRepeat(
        intervalMillis = 1000L,
        key = "start_sign_in_click",
        toastOnBlocked = false
    )
    private fun onSignInClick(view: View) {
        mViewModel.getMain("18507174506")
    }

    private fun updateVerifyCode() {
        mBinding.ivCode.setImageBitmap(RandomverificationCode.instance?.createBitmap())
    }

    fun isDeviceFolded(context: Context): Boolean {
        val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val (width, height) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            bounds.width() to bounds.height()
        } else {
            val metrics = context.resources.displayMetrics
            metrics.widthPixels to metrics.heightPixels
        }
        // 计算屏幕高度和宽度的比例
        val ratio = height.toFloat() / width.toFloat()
        // 如果比例小于某个阈值，则表示设备处于折叠态
        Log.i("TAG", "isDeviceFolded: $ratio")
        if (ratio < 1.2) {
            Log.i("TAG", "isDeviceFolded=展开 ")
            return true
        } else {
            Log.i("TAG", "isDeviceFolded=折叠 ")
        }
        return false
    }

}
