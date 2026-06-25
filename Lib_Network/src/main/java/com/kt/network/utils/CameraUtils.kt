package com.kt.network.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.ImageView
import androidx.core.content.FileProvider
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


/**
 * @author 浩楠
 *
 * @date 2023/5/25-17:48.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO 相机、相册工具类
 */
object CameraUtils {
    fun getUriForFile(context: Context, file: File): Uri {
        file.parentFile?.takeIf { !it.exists() }?.mkdirs()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.myFileProvider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
    }

    fun copyUriToFile(context: Context, sourceUri: Uri, targetFile: File): File? {
        return try {
            targetFile.parentFile?.takeIf { !it.exists() }?.mkdirs()
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            targetFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 判断sdcard是否被挂载
     */
    fun hasSdcard(): Boolean {
        return Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
    }

    /**
     * 更改图片显示角度
     * @param filepath
     * @param orc_bitmap
     * @param iv
     */
    fun ImgUpdateDirection(filepath: String?, orc_bitmap: Bitmap?, iv: ImageView) {
        //图片旋转的角度
        val imagePath = filepath ?: return
        var orcBitmap = orc_bitmap ?: return
        var digree = 0
        try {
            val exif = ExifInterface(imagePath)
            // 读取图片中相机方向信息
            val ori = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            digree = when (ori) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
            //如果图片不为0
            if (digree != 0) {
                // 旋转图片
                val m = Matrix()
                m.postRotate(digree.toFloat())
                orcBitmap = Bitmap.createBitmap(
                    orcBitmap, 0, 0, orcBitmap.width,
                    orcBitmap.height, m, true
                )
            }
            iv.setImageBitmap(orcBitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 比例压缩
     * @param image
     * @return
     */
    fun compression(image: Bitmap): Bitmap? {
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (outputStream.toByteArray().size / 1024 > 1024) {
            //重置outputStream即清空outputStream
            outputStream.reset()
            //这里压缩50%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        }
        var inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val options = BitmapFactory.Options()
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        options.inJustDecodeBounds = true
        var bitmap = BitmapFactory.decodeStream(inputStream, null, options)
        options.inJustDecodeBounds = false
        val outWidth = options.outWidth
        val outHeight = options.outHeight
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        val height = 800f //这里设置高度为800f
        val width = 480f //这里设置宽度为480f

        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        var zoomRatio = 1 //be=1表示不缩放
        if (outWidth > outHeight && outWidth > width) { //如果宽度大的话根据宽度固定大小缩放
            zoomRatio = (options.outWidth / width).toInt()
        } else if (outWidth < outHeight && outHeight > height) { //如果高度高的话根据宽度固定大小缩放
            zoomRatio = (options.outHeight / height).toInt()
        }
        if (zoomRatio <= 0) {
            zoomRatio = 1
        }
        options.inSampleSize = zoomRatio //设置缩放比例
        options.inPreferredConfig = Bitmap.Config.RGB_565 //降低图片从ARGB888到RGB565
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        inputStream = ByteArrayInputStream(outputStream.toByteArray())
        //压缩好比例大小后再进行质量压缩
        bitmap = BitmapFactory.decodeStream(inputStream, null, options)
        return bitmap
    }
}
