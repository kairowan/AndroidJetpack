package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import com.example.basemodel.base.baseact.BaseActivity
import com.ghn.cocknovel.databinding.ActivitySwitchBinding
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.aop.page.PageAccessGuard
import com.ghn.routermodule.feature.FeatureKeys
import com.therouter.router.Route

@Route(path = RouterPath.Setting.FONT)
@PageAccessGuard(
    featureKey = FeatureKeys.FONT_SETTINGS,
    featureBlockedMessage = "字体设置功能暂未开放"
)
class SwitchActivity : BaseActivity<ActivitySwitchBinding, BookStoreViewModel>() {


//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(savedInstanceState: Bundle?): ActivitySwitchBinding =
        ActivitySwitchBinding.inflate(layoutInflater)

    override fun useCommonTitleBar(): Boolean = true

    override fun commonTitleBarTitle(): CharSequence = "字体设置"

    override fun initParam() {


    }

    override fun initView() {
    }

    override fun initViewObservable() {

    }

    override fun initData() {

    }
}
