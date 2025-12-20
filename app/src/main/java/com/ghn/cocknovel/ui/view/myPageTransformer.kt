package com.ghn.cocknovel.ui.view

import android.view.View
import androidx.viewpager.widget.ViewPager

/**
 * @author 浩楠
 *
 * @date 2025/4/12-13:56
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
class myPageTransformer : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val pageWidth = page.width
        val pageHeight = page.height

        when {
            position < -1 -> {
                page.alpha = 0f
            }
            position <= 0 -> {
                page.alpha = 1f
                page.translationX = 0f
                page.rotationY = 0f
            }
            position <= 1 -> {
                page.alpha = 1 - position
                page.translationX = pageWidth * -position
                page.rotationY = 90f * position
            }
            else -> {
                page.alpha = 0f
            }
        }
    }
}
