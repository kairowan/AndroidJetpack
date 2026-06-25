package com.ghn.cocknovel.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.basemodel.base.basefra.BaseFragment
import com.ghn.cocknovel.databinding.FragmentMineBinding
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.ghn.lib.base.aop.permission.capability.RequireCameraPermission
import com.ghn.lib.base.aop.permission.capability.RequireImageReadPermission
import com.ghn.routermodule.auth.LoginRequired
import com.kt.network.utils.BitmapUtils
import com.kt.network.utils.CameraUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class MineFragment : BaseFragment<FragmentMineBinding, BookStoreViewModel>() {
    //存储拍完照后的图片
    private var outputImagePath: File? = null

    private var base64Pic: String? = null
    private var orc_bitmap: Bitmap? = null

    //Glide请求图片选项配置
    private val requestOptions = RequestOptions.circleCropTransform()
        .diskCacheStrategy(DiskCacheStrategy.NONE) //不做磁盘缓存
        .skipMemoryCache(true) //不做内存缓存

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) {
            return@registerForActivityResult
        }
        outputImagePath?.absolutePath?.let(::displayImage) ?: showMsg("图片获取失败")
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        handleSelectedImage(uri)
    }

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
        mBinding.ivToolbarMine.setOnClickListener {
            mViewModel.getsetImage()
        }

        mBinding.rounIcon.setOnClickListener {
            showAvatarActionDialog()
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

    @LoginRequired(message = "请先登录后再更换头像")
    private fun showAvatarActionDialog() {
        val items = arrayOf("拍照", "从相册选择")
        AlertDialog.Builder(requireContext())
            .setTitle("更换头像")
            .setItems(items) { dialog, which ->
                when (which) {
                    0 -> requestAvatarCameraPermission()
                    1 -> requestAvatarAlbumPermission()
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    @RequireCameraPermission(tag = "mine_avatar_camera_permission")
    private fun requestAvatarCameraPermission() {
        takePhoto()
    }

    @RequireImageReadPermission(tag = "mine_avatar_album_permission")
    private fun requestAvatarAlbumPermission() {
        openAlbum()
    }

    /**
     * 拍照
     */
    @SuppressLint("SimpleDateFormat")
    private fun takePhoto() {
        val timeStampFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        val filename = timeStampFormat.format(Date())
        outputImagePath = File(avatarCacheDir(), "$filename.jpg")
        val outputUri = CameraUtils.getUriForFile(requireActivity(), outputImagePath!!)
        takePhotoLauncher.launch(outputUri)
    }

    private fun openAlbum() {
        selectImageLauncher.launch("image/*")
    }

    private fun handleSelectedImage(uri: Uri) {
        val timeStampFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        val filename = "album_${timeStampFormat.format(Date())}.jpg"
        val cacheFile = File(avatarCacheDir(), filename)
        val localFile = CameraUtils.copyUriToFile(requireContext(), uri, cacheFile)
        if (localFile == null) {
            showMsg("图片获取失败")
            return
        }
        displayImage(localFile.absolutePath)
    }

    /**
     * 通过图片路径显示图片
     */
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

    private fun avatarCacheDir(): File {
        return requireContext().externalCacheDir ?: requireContext().cacheDir
    }
}
