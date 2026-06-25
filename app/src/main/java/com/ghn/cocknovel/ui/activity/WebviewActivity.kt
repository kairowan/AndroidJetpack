package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.example.basemodel.base.baseact.BaseActivity
import com.ghn.cocknovel.databinding.ActivityWebviewBinding
import com.ghn.cocknovel.ui.view.MyWebView
import com.ghn.cocknovel.viewmodel.RecommendViewModel
import com.ghn.lib.base.web.bridge.WebBridgeCallback
import com.ghn.lib.base.web.bridge.WebBridgeHost
import com.ghn.lib.base.web.bridge.WebBridgeModules
import com.ghn.lib.base.web.bridge.WebBridgeRegistrar
import com.ghn.routermodule.RouterParams
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.WebPageRequest
import com.ghn.routermodule.WebPageRouteContract
import com.ghn.routermodule.resolveForNavigation
import com.ghn.routermodule.aop.route.RequiredRouteParam
import com.therouter.router.Autowired
import com.therouter.router.Route

/**
 * @author 浩楠
 *
 * @date 2026/6/24
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用网页容器页，负责承载普通 Web 页面与受信任 Bridge 页面。
 */
@Route(path = RouterPath.Web.WEBVIEW)
class WebviewActivity : BaseActivity<ActivityWebviewBinding, RecommendViewModel>() {

    companion object {
        private const val TAG = "WebviewActivity"
        private const val DEFAULT_PAGE_TITLE = "网页浏览"
    }

    @field:RequiredRouteParam(
        routeKey = RouterParams.KEY_WBE_URL,
        notBlank = true,
        message = "网页地址缺失"
    )
    @Autowired(name = RouterParams.KEY_WBE_URL)
    lateinit var webUrl: String

    @Autowired(name = RouterParams.KEY_WEB_ENABLE_BRIDGE)
    var enableBridge: Boolean = false

    @Autowired(name = RouterParams.KEY_WEB_BRIDGE_GROUPS)
    var bridgeGroupsRaw: String? = null

    private val pageRequest: WebPageRequest by lazy {
        WebPageRouteContract.decode(
            url = webUrl,
            enableBridge = enableBridge,
            bridgeGroupsRaw = bridgeGroupsRaw
        )
    }

    private val effectivePageRequest: WebPageRequest by lazy {
        pageRequest.resolveForNavigation().also { effectiveRequest ->
            if (pageRequest.enableBridge && !effectiveRequest.enableBridge) {
                Log.w(TAG, "disable bridge for untrusted or invalid request url=${pageRequest.url}")
            }
        }
    }

    override fun initContentView(savedInstanceState: Bundle?): ActivityWebviewBinding =
        ActivityWebviewBinding.inflate(layoutInflater)

    override fun useCommonTitleBar(): Boolean = true

    override fun commonTitleBarTitle(): CharSequence = DEFAULT_PAGE_TITLE


    override fun initParam() {

    }

    override fun initView() {
        if (effectivePageRequest.enableBridge) {
            installBridge()
        }
        installBackPressHandler()
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
                setCommonTitleBarTitle(title)
            }

        })
        mBinding.homeWebview.loadUrl(effectivePageRequest.url)
    }

    override fun initViewObservable() {

    }

    override fun initData() {

    }

    override fun onDestroy() {
        mBinding.homeWebview.destory()
        super.onDestroy()
    }

    private fun installBridge() {
        val host = object : WebBridgeHost {
            override val activity = this@WebviewActivity
            override val enabledBridgeGroups: Set<String> = effectivePageRequest.bridgeGroups

            override fun currentUrl(): String? = mBinding.homeWebview.currentUrl()

            override fun closePage() {
                finish()
            }

            override fun callHandler(
                handlerName: String,
                data: String?,
                callback: ((String) -> Unit)?
            ) {
                mBinding.homeWebview.callHandler(handlerName, data, callback)
            }

            override fun sendToWeb(data: String, callback: ((String) -> Unit)?) {
                mBinding.homeWebview.sendToWeb(data, callback)
            }
        }
        val registrar = object : WebBridgeRegistrar {
            override fun handler(
                name: String,
                block: (data: String?, callback: WebBridgeCallback) -> Unit
            ) {
                mBinding.homeWebview.registerHandler(name) { data, rawCallback ->
                    block(data, rawCallback.asWebBridgeCallback())
                }
            }

            override fun defaultHandler(
                block: (data: String?, callback: WebBridgeCallback) -> Unit
            ) {
                mBinding.homeWebview.setDefaultHandler { data, rawCallback ->
                    block(data, rawCallback.asWebBridgeCallback())
                }
            }
        }
        registrar.defaultHandler { _, callback ->
            callback.failure("unsupported bridge action")
        }
        WebBridgeModules.resolve(
            url = effectivePageRequest.url,
            enabledGroups = host.enabledBridgeGroups
        )
            .forEach { module ->
                module.register(registrar, host)
            }
    }

    private fun installBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!mBinding.homeWebview.goBackIfPossible()) {
                    finish()
                }
            }
        })
    }

    override fun onCommonTitleBarLeftClick() {
        if (!mBinding.homeWebview.goBackIfPossible()) {
            finish()
        }
    }
}

private fun (((String) -> Unit)?).asWebBridgeCallback(): WebBridgeCallback {
    return object : WebBridgeCallback {
        override fun reply(payload: String) {
            this@asWebBridgeCallback?.invoke(payload)
        }
    }
}
