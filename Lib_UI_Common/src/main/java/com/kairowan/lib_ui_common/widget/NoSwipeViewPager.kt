package com.kairowan.lib_ui_common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager




/**
 * @author 浩楠
 *
 * @date 2023/1/10   11:01.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述:
 */
//禁止滑动的viewpager
class NoSwipeViewPager(context: Context?, attributeSet: AttributeSet?) :
    ViewPager(context!!, attributeSet) {
    private var canSwipe = true

    //是否禁止滑动
    fun setCanSwipe(canSwipe: Boolean) {
        this.canSwipe = canSwipe
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return canSwipe && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return canSwipe && super.onInterceptTouchEvent(ev)
    }
}