package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import android.util.Log
import com.example.basemodel.base.baseact.BaseActivity
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.databinding.ActivityWebviewBinding
import com.ghn.cocknovel.ui.view.MyWebView
import com.ghn.cocknovel.viewmodel.RecommendViewModel
import com.ghn.routermodule.RouterParams
import com.ghn.routermodule.RouterPath
import com.therouter.router.Autowired
import com.therouter.router.Route

@Route(path = RouterPath.Web.WEBVIEW)
class WebviewActivity : BaseActivity<ActivityWebviewBinding, RecommendViewModel>() {

    @Autowired(name = RouterParams.KEY_WBE_URL)
    lateinit var webUrl: String

//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(savedInstanceState: Bundle?): ActivityWebviewBinding =
        ActivityWebviewBinding.inflate(layoutInflater)


    override fun initParam() {

    }

    override fun initView() {
        //webview加载成功和失败的回调
        mBinding.homeWebview.setOnLoadStatueListener(object : MyWebView.OnWebLoadStatusListener {
            override fun error() {
                Log.i("TAG", "error: 失败")
            }

            override fun success() {
                Log.i("TAG", "success: 成功")
            }

            override fun onTitle(title: String) {
                Log.i("TAG", "onTitle: 标题")
            }

        })
        mBinding.homeWebview.loadUrl(webUrl)
        mBinding.homeWebview.canGoBack()
    }

    override fun initViewObservable() {

    }

    override fun initData() {

    }
}