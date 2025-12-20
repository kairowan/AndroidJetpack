package com.kairowan.lib_ui_common.ext

import android.widget.Toast
import androidx.annotation.DrawableRes
import com.kairowan.lib_ui_common.helper.ToastHelper


/**
 * @author 浩楠
 * @date 2025/7/22
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO
 */
fun showToast(
    message: CharSequence,
    duration: Int = Toast.LENGTH_SHORT,
    @DrawableRes iconRes: Int? = null
) {
    ToastHelper.showToast(message, duration, iconRes)
}