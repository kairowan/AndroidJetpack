package com.ghn.cocknovel.ui.activity

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.example.basemodel.base.baseact.BaseActivity
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.ActivityStartBinding
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.kt.network.utils.RandomverificationCode


class StartActivity : BaseActivity<ActivityStartBinding, BookStoreViewModel>() {
//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(savedInstanceState: Bundle?): ActivityStartBinding =
        ActivityStartBinding.inflate(layoutInflater)


    override fun initParam() {


    }

    override fun initView() {
        mBinding.ivCode.setImageBitmap(RandomverificationCode.instance?.createBitmap())
        mBinding.ivCode.setOnClickListener {
            mBinding.ivCode.setImageBitmap(RandomverificationCode.instance?.createBitmap())

        }
        mBinding.btSignIn.setOnClickListener {
            mViewModel.getMain("18507174506")
//            if (!TextUtils.isEmpty(mBinding?.startEdtextName?.text.toString()) && !TextUtils.isEmpty(mBinding?.startEdtextPassword?.text.toString())){
//
//            }else{
//                showMsg("账号密码不能为空")
//            }
        }
    }

    override fun initViewObservable() {

    }

    override fun initData() {
        isDeviceFolded(this)
    }

    fun isDeviceFolded(context: Context): Boolean {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        display.getMetrics(metrics)
        // 计算屏幕高度和宽度的比例
        val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
        // 如果比例小于某个阈值，则表示设备处于折叠态
        Log.i("TAG", "isDeviceFolded: $ratio")
        if (ratio < 1.2) {
            Log.i("TAG", "isDeviceFolded=展开 ")
            return true
        } else {
            Log.i("TAG", "isDeviceFolded=折叠 ")
        }
        return false
    }

}