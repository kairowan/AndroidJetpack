package com.ghn.cocknovel.utils

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.ghn.cocknovel.R
import com.kairowan.lib_ui_common.ext.dp
import com.ghn.routermodule.AppRouter
import com.kt.ktmvvm.lib.BuildConfig


/**
 * @author 浩楠
 * @date 2025/7/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO
 */
object DebugEntryHelper {

    fun attachToActivity(activity: Activity) {
        if (!BuildConfig.DEBUG) return // 仅调试环境生效

        val context = activity
        val rootView = activity.window.decorView as? ViewGroup ?: return

        val debugBtn = TextView(context).apply {
            text = "抓包"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.debug_button_bg) // 圆角半透明背景
            setPadding(16, 8, 16, 8)
            alpha = 0.8f
            setOnClickListener {
                Log.i("attachToActivity", "点击事件: ")
                AppRouter.openNetworkCapture()
//                val className = "cn.coderpig.cp_network_capture.ui.activity.NetworkCaptureActivity"
//                try {
//                    val clazz = Class.forName(className)
//                    val intent = Intent(context, clazz)
//                    context.startActivity(intent)
//                } catch (e: Exception) {
//                    Toast.makeText(context, "抓包组件未集成", Toast.LENGTH_SHORT).show()
//                }
            }
        }

        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END or Gravity.TOP
            topMargin = 100.dp
            marginEnd = 16.dp
        }

        rootView.addView(debugBtn, lp)
    }
}
