package com.ghn.cocknovel.utils

import android.content.Context
import android.graphics.Typeface
import java.lang.reflect.Field

/**
 * @author 浩楠
 *
 * @date 2023/1/16   10:44.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述:
 */
object ChangeDefaultFontUtils {
    fun setDefaultFont(
        context: Context,
        staticTypefaceFieldName: String,
        fontAssetName: String
    ) {
        val regular = Typeface.createFromAsset(
            context.assets,
            fontAssetName
        )
        replaceFont(staticTypefaceFieldName, regular)
    }


    private fun replaceFont(
        staticTypefaceFieldName: String,
        newTypeface: Typeface?
    ) {
        try {
            val staticField: Field = Typeface::class.java
                .getDeclaredField(staticTypefaceFieldName)
            staticField.isAccessible = true
            staticField.set(null, newTypeface)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}
