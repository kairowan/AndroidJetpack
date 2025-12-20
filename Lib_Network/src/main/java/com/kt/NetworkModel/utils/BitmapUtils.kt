package com.kt.NetworkModel.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * @author 浩楠
 *
 * @date 2023/5/25-17:47.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO Bitmap工具类
 */
object BitmapUtils {
    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    fun bitmapToBase64(bitmap: Bitmap?): String? {
        var result: String? = null
        var baos: ByteArrayOutputStream? = null
        try {
            if (bitmap != null) {
                baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                baos.flush()
                baos.close()
                val bitmapBytes = baos.toByteArray()
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (baos != null) {
                    baos.flush()
                    baos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return result
    }

    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    fun base64ToBitmap(base64Data: String?): Bitmap {
        val bytes = Base64.decode(base64Data, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /**
     * url转bitmap
     * @param url
     * @return
     */
    fun urlToBitmap(url: String?): Bitmap? {
        val bitmap = arrayOf<Bitmap?>(null)
        Thread {
            var imageurl: URL? = null
            try {
                imageurl = URL(url)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            try {
                val conn =
                    imageurl!!.openConnection() as HttpURLConnection
                conn.doInput = true
                conn.connect()
                val `is` = conn.inputStream
                bitmap[0] = BitmapFactory.decodeStream(`is`)
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
        return bitmap[0]
    }
}

