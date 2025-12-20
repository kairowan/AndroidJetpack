package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import com.example.basemodel.base.baseact.BaseActivity
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.ActivitySetBinding
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar


class SetActivity : BaseActivity<ActivitySetBinding, BookStoreViewModel>() {
//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(savedInstanceState: Bundle?): ActivitySetBinding =
        ActivitySetBinding.inflate(layoutInflater)


    override fun initParam() {

    }

    override fun initView() {
        mBinding.titlebar.setOnTitleBarListener(object : OnTitleBarListener {
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