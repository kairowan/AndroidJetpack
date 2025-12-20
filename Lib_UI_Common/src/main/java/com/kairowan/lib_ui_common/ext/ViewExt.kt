package com.kairowan.lib_ui_common.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @author 浩楠
 * @date 2025/6/11 14:40
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO View扩展函数
 */
// 显示 View
fun View.show() {
    isVisible = true
}

// GONE 隐藏 View
fun View.hide() {
    isGone = true
}

// INVISIBLE 隐藏 View（仍占布局位置）
fun View.invisible() {
    isInvisible = true
}

// 可见性链式设置
fun View.setVisible(visible: Boolean): View {
    isVisible = visible
    return this
}

fun View.setGone(gone: Boolean): View {
    isGone = gone
    return this
}

fun View.setInvisible(invisible: Boolean): View {
    isInvisible = invisible
    return this
}

// 根据条件显示/隐藏
fun View.visibleIf(condition: Boolean) {
    isVisible = condition
}

fun View.goneIf(condition: Boolean) {
    isGone = condition
}

// 延迟显示/隐藏
fun View.showDelayed(delayMillis: Long = 300L) {
    postDelayed({ isVisible = true }, delayMillis)
}

fun View.hideDelayed(delayMillis: Long = 300L) {
    postDelayed({ isGone = true }, delayMillis)
}

// 启用/禁用 + 透明变灰处理
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
fun View.enableWithAlpha(enable: Boolean, disabledAlpha: Float = 0.5f) {
    isEnabled = enable
    alpha = if (enable) 1f else disabledAlpha
}

// 单个 View 防抖点击
fun View.setDebouncedClick(interval: Long = 800L, action: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            action(it)
        }
    }
}

// 多个 View 设置防抖点击
fun setDebouncedClick(vararg views: View, interval: Long = 800L, action: (View) -> Unit) {
    views.forEach { it.setDebouncedClick(interval, action) }
}

// 获取 View 在屏幕中的位置（用于引导层、提示浮窗等）
fun View.getLocationOnScreen(): Pair<Int, Int> {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return location[0] to location[1]
}

/**
 * TextView 扩展函数
 */

// 设置文本并支持 null 安全
fun TextView.setSafeText(text: CharSequence?) {
    this.text = text ?: ""
}

// 设置 HTML 富文本内容
fun TextView.setHtmlText(html: String?) {
    this.text = HtmlCompat.fromHtml(html ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
}

// 设置文字颜色（通过 colorInt）
fun TextView.setTextColorInt(@ColorInt color: Int) {
    this.setTextColor(color)
}

// 设置文字颜色（通过 colorRes）
fun TextView.setTextColorRes(@ColorRes resId: Int) {
    this.setTextColor(ContextCompat.getColor(context, resId))
}

// 设置背景颜色（通过 colorInt）
fun TextView.setBackgroundColorInt(@ColorInt color: Int) {
    this.setBackgroundColor(color)
}

// 设置背景颜色（通过 colorRes）
fun TextView.setBackgroundColorRes(@ColorRes resId: Int) {
    this.setBackgroundColor(ContextCompat.getColor(context, resId))
}

// 设置圆角背景（纯色 + 圆角）
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun TextView.setRoundRectBackground(@ColorInt bgColor: Int, radiusDp: Float) {
    val drawable = GradientDrawable()
    drawable.setColor(bgColor)
    drawable.cornerRadius = radiusDp * context.resources.displayMetrics.density
    background = drawable
}

// 设置圆角边框背景（边框 + 背景色 + 圆角）
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun TextView.setStrokeRoundRect(
    @ColorInt strokeColor: Int,
    strokeWidthDp: Float,
    @ColorInt fillColor: Int,
    radiusDp: Float
) {
    val drawable = GradientDrawable()
    drawable.setColor(fillColor)
    drawable.setStroke(
        (strokeWidthDp * context.resources.displayMetrics.density).toInt(),
        strokeColor
    )
    drawable.cornerRadius = radiusDp * context.resources.displayMetrics.density
    background = drawable
}

// 设置文字颜色状态列表（用于点击/不可用状态）
fun TextView.setTextColorStateList(colorStateList: ColorStateList) {
    this.setTextColor(colorStateList)
}

// 基础函数：直接设置 Drawable
fun TextView.setDrawables(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null
) {
    setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
}

// 快捷函数：通过资源 ID 设置 Drawable
fun TextView.setDrawablesRes(
    @DrawableRes leftRes: Int = 0,
    @DrawableRes topRes: Int = 0,
    @DrawableRes rightRes: Int = 0,
    @DrawableRes bottomRes: Int = 0
) {
    val l = if (leftRes != 0) context.drawableCompat(leftRes) else null
    val t = if (topRes != 0) context.drawableCompat(topRes) else null
    val r = if (rightRes != 0) context.drawableCompat(rightRes) else null
    val b = if (bottomRes != 0) context.drawableCompat(bottomRes) else null
    setDrawables(l, t, r, b)
}

// 图标与文字间距（dp）
fun TextView.setDrawablePaddingDp(paddingDp: Float) {
    compoundDrawablePadding = (paddingDp * context.resources.displayMetrics.density).toInt()
}

// 辅助方法：Context 获取 Drawable
private fun Context.drawableCompat(@DrawableRes resId: Int): Drawable? =
    ContextCompat.getDrawable(this, resId)

/**
 * EditText 扩展函数
 */

// 输入监听
fun EditText.afterTextChanged(after: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = after(s.toString())
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

// 设置文本并移动光标到末尾
fun EditText.setTextWithCursorEnd(text: String?) {
    setText(text ?: "")
    setSelection(this.text.length)
}

// 快速清空
fun EditText.clearText() {
    setText("")
}

fun EditText.setDrawables(
    left: Drawable? = null,
    top: Drawable? = null,
    right: Drawable? = null,
    bottom: Drawable? = null
) {
    setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
}

fun EditText.setDrawablesRes(
    @DrawableRes leftRes: Int = 0,
    @DrawableRes topRes: Int = 0,
    @DrawableRes rightRes: Int = 0,
    @DrawableRes bottomRes: Int = 0
) {
    val l = if (leftRes != 0) context.drawableCompat(leftRes) else null
    val t = if (topRes != 0) context.drawableCompat(topRes) else null
    val r = if (rightRes != 0) context.drawableCompat(rightRes) else null
    val b = if (bottomRes != 0) context.drawableCompat(bottomRes) else null
    setDrawables(l, t, r, b)
}

fun EditText.setDrawablePaddingDp(paddingDp: Float) {
    compoundDrawablePadding = (paddingDp * context.resources.displayMetrics.density).toInt()
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun EditText.setRoundRectBackground(@ColorInt bgColor: Int, radiusDp: Float) {
    val drawable = GradientDrawable()
    drawable.setColor(bgColor)
    drawable.cornerRadius = radiusDp * context.resources.displayMetrics.density
    background = drawable
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun EditText.setStrokeRoundRect(
    @ColorInt strokeColor: Int,
    strokeWidthDp: Float,
    @ColorInt fillColor: Int,
    radiusDp: Float
) {
    val drawable = GradientDrawable()
    drawable.setColor(fillColor)
    drawable.setStroke(
        (strokeWidthDp * context.resources.displayMetrics.density).toInt(),
        strokeColor
    )
    drawable.cornerRadius = radiusDp * context.resources.displayMetrics.density
    background = drawable
}

/**
 * RecyclerView 扩展函数
 */

// 滚动到顶部
fun RecyclerView.scrollToTop() {
    scrollToPosition(0)
}

// 快速设置纵向列表布局
fun RecyclerView.setLinearVertical() {
    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
}

// 快速设置横向列表布局
fun RecyclerView.setLinearHorizontal() {
    layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
}

// 判断是否滑动到底部
fun RecyclerView.isScrolledToBottom(): Any {
    val layoutManager = layoutManager as? LinearLayoutManager ?: return false
    val lastVisible = layoutManager.findLastVisibleItemPosition()
    val total = layoutManager.itemCount
    return lastVisible.compareTo(total - 1)
}

/**
 * Button 扩展函数
 */

// 防抖点击（复用 View 的）
fun Button.setDebouncedClick(interval: Long = 800L, action: (View) -> Unit) {
    this.setDebouncedClick(interval, action)
}

// 设置启用状态 + 透明度（可选）
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
fun Button.setEnableAlpha(enable: Boolean, disabledAlpha: Float = 0.5f) {
    isEnabled = enable
    alpha = if (enable) 1f else disabledAlpha
}

/**
 * 软键盘控制扩展
 */

// 隐藏键盘（从 view 上）
@SuppressLint("WrongConstant")
@RequiresApi(Build.VERSION_CODES.CUPCAKE)
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0)
}

// 显示键盘
@SuppressLint("WrongConstant")
@RequiresApi(Build.VERSION_CODES.CUPCAKE)
fun View.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

// 隐藏键盘（从 Activity）
@RequiresApi(Build.VERSION_CODES.CUPCAKE)
fun Activity.hideKeyboard() {
    currentFocus?.hideKeyboard()
}