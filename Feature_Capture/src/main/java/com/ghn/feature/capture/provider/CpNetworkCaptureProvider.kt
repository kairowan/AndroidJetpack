package com.ghn.feature.capture.provider

import NetworkCapture
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.ghn.feature.capture.utils.LogHelper.TAG

/**
 * Author: zpj
 * Date: 2023-09-05 15:31
 * Desc:
 */

class CpNetworkCaptureProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val context = context
        if (context == null) {
            Log.e(TAG, "CpNetworkCapture库初始化Context失败")
        } else {
            Log.e(TAG, context.packageName)
            NetworkCapture.init(context)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0

}