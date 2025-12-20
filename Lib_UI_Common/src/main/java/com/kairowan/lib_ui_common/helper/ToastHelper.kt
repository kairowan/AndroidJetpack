package com.kairowan.lib_ui_common.helper

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.kairowan.lib_ui_common.R

/**
 * @author 浩楠
 *
 * @date 2023/8/30-17:02.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
@SuppressLint("StaticFieldLeak")
object ToastHelper {

    private lateinit var appContext: Context
    private var toast: Toast? = null
    private var textView: TextView? = null
    private var imageView: ImageView? = null

    private var lastMessage: CharSequence? = null
    private var lastToastTime: Long = 0
    private const val MIN_INTERVAL = 2000L // 2 秒内不重复

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun showToast(
        message: CharSequence,
        duration: Int = Toast.LENGTH_SHORT,
        @DrawableRes iconRes: Int? = null
    ) {
        if (!::appContext.isInitialized) {
            throw IllegalStateException("ToastHelper not initialized. Call ToastHelper.init(appContext)")
        }

        val now = System.currentTimeMillis()
        if (message == lastMessage && now - lastToastTime < MIN_INTERVAL) {
            return // 阻止重复弹出
        }

        lastMessage = message
        lastToastTime = now

        if (toast == null) {
            toast = Toast(appContext)
            val view = LayoutInflater.from(appContext).inflate(R.layout.toast_utlis, null)
            textView = view.findViewById(R.id.toast_message)
            imageView = view.findViewById(R.id.toast_image)
            toast?.view = view
        }

        textView?.text = message
        imageView?.visibility = if (iconRes != null) View.VISIBLE else View.GONE

        iconRes?.let {
            Glide.with(appContext).load(it).into(imageView!!)
        }

        toast?.duration = duration
        toast?.setGravity(Gravity.CENTER, 0, 0)
        toast?.show()
    }
}