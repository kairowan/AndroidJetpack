package com.ghn.cocknovel.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.basemodel.base.basefra.BaseFragment
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.FragmentMineBinding
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.kt.NetworkModel.utils.BitmapUtils
import com.kt.NetworkModel.utils.CameraUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class MineFragment : BaseFragment<FragmentMineBinding, BookStoreViewModel>() {
    //存储拍完照后的图片
    private var outputImagePath: File? = null

    //启动相机标识
    val TAKE_PHOTO = 1

    //启动相册标识
    val SELECT_PHOTO = 2

    private var base64Pic: String? = null
    private var orc_bitmap: Bitmap? = null

    //Glide请求图片选项配置
    private val requestOptions = RequestOptions.circleCropTransform()
        .diskCacheStrategy(DiskCacheStrategy.NONE) //不做磁盘缓存
        .skipMemoryCache(true) //不做内存缓存

//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMineBinding = FragmentMineBinding.inflate(inflater,container,false)


    override fun initParam() {

    }

    override fun initView() {

        mBinding.rounIcon.setOnClickListener {
            // 打开自定义dialog
//            openDialog()
//            context.let { it1 -> ButtomDialogView(it1,true,true).show() }
        }
    }

    override fun initData() {
        mBinding.let {
            Glide.with(this).load(com.ghn.lib.base.R.mipmap.ic_my_handes)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(it.rounIcon)
        }
    }

    override fun initViewObservable() {

    }

//    private fun openDialog() {
//        val headDialog = context.let { ButtomDialogView(it, false, true) }
//        headDialog.show()
//        headDialog.setClicklistener(object : ButtomDialogView.ClickListenerInterface {
//            override fun doGetCamera() {
//                //相机
//                headDialog.dismiss()
//                activity.let {
//                    XXPermissions.with(it)
//                        .permission(Permission.CAMERA).permission(Permission.READ_MEDIA_IMAGES)
//                        .request(object : OnPermissionCallback {
//                            override fun onGranted(
//                                permissions: MutableList<String>,
//                                allGranted: Boolean
//                            ) {
//                                if (!allGranted) {
//                                    showMsg("获取部分权限成功，但部分权限未正常授予")
//                                    return
//                                } else {
//                                    takePhoto()
//                                }
//                            }
//
//                            override fun onDenied(
//                                permissions: MutableList<String>,
//                                doNotAskAgain: Boolean
//                            ) {
//                                if (doNotAskAgain) {
//                                    showMsg("被永久拒绝授权，请手动授权")
//                                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
//                                    XXPermissions.startPermissionActivity(activity!!, permissions)
//                                } else {
//                                    showMsg("获取权限失败")
//                                }
//                            }
//                        })
//                }
//            }
//
//            override fun doGetPic() {
//                //相册
//                headDialog.dismiss()
//                activity.let {
//                    XXPermissions.with(it)
//                        .permission(Permission.CAMERA).permission(Permission.READ_MEDIA_IMAGES)
//                        .request(object : OnPermissionCallback {
//                            override fun onGranted(
//                                permissions: MutableList<String>,
//                                allGranted: Boolean
//                            ) {
//                                if (!allGranted) {
//                                    showMsg("获取部分权限成功，但部分权限未正常授予")
//                                    return
//                                } else {
//                                    openAlbum()
//                                }
//                            }
//
//                            override fun onDenied(
//                                permissions: MutableList<String>,
//                                doNotAskAgain: Boolean
//                            ) {
//                                if (doNotAskAgain) {
//                                    showMsg("被永久拒绝授权，请手动授权")
//                                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
//                                    XXPermissions.startPermissionActivity(activity!!, permissions)
//                                } else {
//                                    showMsg("获取权限失败")
//                                }
//                            }
//                        })
//                }
//            }
//
//            override fun doCancel() {
//                headDialog.dismiss()
//            }
//        })
//
//    }


    /**
     * 拍照
     */
    @SuppressLint("SimpleDateFormat")
    private fun takePhoto() {
        val timeStampFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        val filename = timeStampFormat.format(Date())
        outputImagePath = File(activity?.getExternalCacheDir(), "$filename.jpg")
        val takePhotoIntent =
            activity.let { CameraUtils.getTakePhotoIntent(it!!, outputImagePath!!) }
        // 开启一个带有返回值的Activity，请求码为TAKE_PHOTO
        // 开启一个带有返回值的Activity，请求码为TAKE_PHOTO
        startActivityForResult(takePhotoIntent, TAKE_PHOTO)
    }

    private fun openAlbum() {
        startActivityForResult(CameraUtils.getselectPhotoIntent, SELECT_PHOTO)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                //显示图片
                displayImage(outputImagePath!!.absolutePath)

            }

            SELECT_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                var imagePath: String? = null
                //判断手机系统版本号
                imagePath = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    //4.4及以上系统使用这个方法处理图片
                    activity.let { CameraUtils.getImageOnKitKatPath(data!!, it!!) }
                } else {
                    activity.let { CameraUtils.getImageBeforeKitKatPath(data!!, it!!) }
                }
                //显示图片
                imagePath.let { displayImage(it!!) }
            }

            else -> {}
        }
    }

    /**
     * 通过图片路径显示图片
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun displayImage(imagePath: String) {
        if (!TextUtils.isEmpty(imagePath)) {
            //显示图片
            mBinding.rounIcon.let {
                Glide.with(this).load(imagePath).apply(requestOptions).into(it)
            }
            //压缩图片
            orc_bitmap = CameraUtils.compression(BitmapFactory.decodeFile(imagePath))
            //转Base64
            base64Pic = BitmapUtils.bitmapToBase64(orc_bitmap)
        } else {
            showMsg("图片获取失败")
        }
    }
}
