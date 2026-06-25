package com.ghn.cocknovel.utils

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.ghn.cocknovel.R
import com.ghn.routermodule.aop.debug.DebugOnly
import com.ghn.routermodule.AppRouter
import com.ghn.routermodule.feature.FeatureFlagStore
import com.ghn.routermodule.feature.FeatureKeys
import com.kairowan.lib_ui_common.ext.dp
import com.kairowan.lib_ui_common.helper.ToastHelper


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

    @DebugOnly
    fun attachToActivity(activity: Activity) {
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
                AppRouter.openNetworkCapture(activity)
            }
            setOnLongClickListener {
                showFeatureToggleDialog(activity)
                true
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

    private fun showFeatureToggleDialog(activity: Activity) {
        val items = arrayOf("抓包功能", "字体设置")
        val keys = arrayOf(FeatureKeys.NETWORK_CAPTURE, FeatureKeys.FONT_SETTINGS)
        val checkedItems = keys.map { FeatureFlagStore.isEnabled(it, true) }.toBooleanArray()
        AlertDialog.Builder(activity)
            .setTitle("功能开关")
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("应用") { dialog, _ ->
                keys.forEachIndexed { index, key ->
                    FeatureFlagStore.setEnabled(key, checkedItems[index])
                }
                dialog.dismiss()
                ToastHelper.showToast("功能开关已更新")
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
