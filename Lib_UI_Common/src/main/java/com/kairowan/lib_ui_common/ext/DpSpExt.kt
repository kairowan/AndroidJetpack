package com.kairowan.lib_ui_common.ext

import android.content.res.Resources
import android.util.TypedValue
/**
 * @author 浩楠
 * @date 2025/6/11 16:59
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 单位换算扩展函数（dp、sp、px 双向）
 */

/**
 * ========== dp → px ==========
 */

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density

/**
 * ========== sp → px ==========
 */

val Int.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

/**
 * ========== px → dp ==========
 */

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Float.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density

/**
 * ========== px → sp ==========
 */

val Int.pxToSp: Int
    get() = (this / Resources.getSystem().displayMetrics.scaledDensity).toInt()

val Float.pxToSp: Float
    get() = this / Resources.getSystem().displayMetrics.scaledDensity

/**
 * ========== dp → sp ==========
 */

val Int.dpToSp: Int
    get() = (this.dp / Resources.getSystem().displayMetrics.scaledDensity).toInt()

val Float.dpToSp: Float
    get() = this.dp / Resources.getSystem().displayMetrics.scaledDensity

/**
 * ========== sp → dp ==========
 */

val Int.spToDp: Int
    get() = (this.sp / Resources.getSystem().displayMetrics.density).toInt()

val Float.spToDp: Float
    get() = this.sp / Resources.getSystem().displayMetrics.density
