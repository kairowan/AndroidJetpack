package com.kt.NetworkModel.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
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
    /**
     * 相机Intent
     * @param context
     * @param outputImagePath
     * @return
     */
    fun getTakePhotoIntent(context: Context, outputImagePath: File): Intent {
        // 激活相机
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val state = Environment.getExternalStorageState()
        if (state == Environment.MEDIA_MOUNTED) {
            val getImageByCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            var mUri = Uri.fromFile(
                File(
                    Environment.getExternalStorageDirectory(),
                    "/DCIM/Camera/"
                            + System.currentTimeMillis().toString() + ".png"
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
            }

            // 判断存储卡是否可以用，可用进行存储
//        if (hasSdcard()) {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                // 从文件中创建uri
//                val uri = Uri.fromFile(outputImagePath)
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//            } else {
//                //兼容android7.0 使用共享文件的形式
//                val contentValues = ContentValues(1)
//                contentValues.put(MediaStore.Images.Media.DATA, outputImagePath.absolutePath)
//                val uri = context.applicationContext.contentResolver.insert(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    contentValues
//                )
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//            }
//        }
        }
        return intent
    }

    /**
     * 相册Intent
     * @return
     */
    val getselectPhotoIntent: Intent
        get() {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            return intent
        }

    /**
     * 判断sdcard是否被挂载
     */
    fun hasSdcard(): Boolean {
        return Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
    }

    /**
     * 4.4及以上系统处理图片的方法
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getImageOnKitKatPath(data: Intent, context: Context): String? {
        var imagePath: String? = null
        val uri = data.data
        Log.d("uri=intent.getData :", "" + uri)
        if (DocumentsContract.isDocumentUri(context, uri)) {
            //数据表里指定的行
            val docId = DocumentsContract.getDocumentId(uri)
            Log.d("getDocumentId(uri) :", "" + docId)
            Log.d("uri.getAuthority() :", "" + uri!!.authority)
            if ("com.android.providers.media.documents" == uri.authority) {
                val id = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath =
                    getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, context)
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(docId)
                )
                imagePath = getImagePath(contentUri, null, context)
            }
        } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
            imagePath = getImagePath(uri, null, context)
        }
        return imagePath
    }

    /**
     * 通过uri和selection来获取真实的图片路径,从相册获取图片时要用
     */
    @SuppressLint("Range")
    fun getImagePath(uri: Uri?, selection: String?, context: Context): String? {
        var path: String? = null
        val cursor = context.contentResolver.query(uri!!, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }

    /**
     * 更改图片显示角度
     * @param filepath
     * @param orc_bitmap
     * @param iv
     */
    fun ImgUpdateDirection(filepath: String?, orc_bitmap: Bitmap?, iv: ImageView) {
        //图片旋转的角度
        var orc_bitmap = orc_bitmap
        var digree = 0
        //根据图片的filepath获取到一个ExifInterface的对象
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(filepath!!)
            if (exif != null) {

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
            }
            //如果图片不为0
            if (digree != 0) {
                // 旋转图片
                val m = Matrix()
                m.postRotate(digree.toFloat())
                orc_bitmap = Bitmap.createBitmap(
                    orc_bitmap!!, 0, 0, orc_bitmap.width,
                    orc_bitmap.height, m, true
                )
            }
            if (orc_bitmap != null) {
                iv.setImageBitmap(orc_bitmap)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            exif = null
        }
    }

    /**
     * 4.4以下系统处理图片的方法
     */
    fun getImageBeforeKitKatPath(
        data: Intent,
        context: Context
    ): String? {
        val uri = data.data
        return getImagePath(uri, null, context)
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

