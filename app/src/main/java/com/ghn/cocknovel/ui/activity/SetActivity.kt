package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import com.example.basemodel.base.baseact.BaseActivity
import com.ghn.cocknovel.databinding.ActivitySetBinding
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.ghn.routermodule.RouterPath
import com.therouter.router.Route


@Route(path = RouterPath.Setting.SETTINGS)
class SetActivity : BaseActivity<ActivitySetBinding, BookStoreViewModel>() {
//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(savedInstanceState: Bundle?): ActivitySetBinding =
        ActivitySetBinding.inflate(layoutInflater)

    override fun useCommonTitleBar(): Boolean = true

    override fun commonTitleBarTitle(): CharSequence = "设置"

    override fun initParam() {

    }

    override fun initView() {
        mBinding.layoutFontSettings.setOnClickListener {
            mViewModel.getSwitchFont()
        }
    }

    override fun initViewObservable() {

    }

    override fun initData() {

    }
}
