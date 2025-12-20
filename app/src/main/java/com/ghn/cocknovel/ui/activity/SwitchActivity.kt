package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import com.example.basemodel.base.baseact.BaseActivity
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.ActivitySwitchBinding
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar

class SwitchActivity : BaseActivity<ActivitySwitchBinding, BookStoreViewModel>() {


//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(savedInstanceState: Bundle?): ActivitySwitchBinding =
        ActivitySwitchBinding.inflate(layoutInflater)


    override fun initParam() {


    }

    override fun initView() {
        mBinding.titleBarSwtich?.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar) {
                finish()

            }

            override fun onTitleClick(titleBar: TitleBar) {

            }

            override fun onRightClick(titleBar: TitleBar) {

            }
        })
    }

    override fun initViewObservable() {

    }

    override fun initData() {

    }
}