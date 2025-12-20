package com.ghn.feature.capture.ui.widget

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.ghn.feature.capture.ui.activity.TextSearchActivity
import com.ghn.feature.capture.utils.fly

import com.google.android.material.snackbar.Snackbar

/**
 * Author: zpj
 * Date: 2023-09-05 15:14
 * Desc: 支持长按复制到剪贴板的TextView
 */
class LongClickCopyTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        setTextIsSelectable(true)
    }

    override fun performClick(): Boolean {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText(null, text))
        Snackbar.make(this, "已复制至剪切板!", Snackbar.LENGTH_SHORT).show()
        return true
    }

    override fun performLongClick(): Boolean {
        NetworkCapture.currentSearchText = text.toString()
        (context as Activity).fly<TextSearchActivity>()
        return true
    }
}