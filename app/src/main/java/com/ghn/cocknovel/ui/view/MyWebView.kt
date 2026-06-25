package com.ghn.cocknovel.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.webkit.ValueCallback
import android.webkit.WebResourceResponse
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.github.lzyzsd.jsbridge.BridgeHandler
import com.github.lzyzsd.jsbridge.BridgeHelper
import com.github.lzyzsd.jsbridge.IWebView
import com.github.lzyzsd.jsbridge.OnBridgeCallback
import com.ghn.cocknovel.R
import com.ghn.lib.upload.picker.UploadPickerHostFragment
import kotlinx.coroutines.launch

/**
 * @author 浩楠
 *
 * @date 2023/9/15-16:20.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: 封装 JsBridge、文件选择和页面加载状态回调的 WebView 容器。
 */
@SuppressLint("SetJavaScriptEnabled")
class MyWebView @JvmOverloads constructor(
    context: Context,
    attributeset: AttributeSet? = null
) : ConstraintLayout(context, attributeset) {

    companion object {
        const val DEFAULT_JS_INTERFACE_NAME = "SSDJsBirdge"
        private const val TAG = "MyWebView"
    }

    private val webview: WebView
    private val bridgeHandlers = LinkedHashMap<String, (String?, ((String) -> Unit)?) -> Unit>()
    private var defaultBridgeHandler: ((String?, ((String) -> Unit)?) -> Unit)? = null
    private var bridgeHelper: BridgeHelper
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var isLastLoadSuccess = false//是否成功加载完成过web，成功过后的网络异常 不改变web
    private var isError = false

    init {
        val rootView =
            LayoutInflater.from(context).inflate(R.layout.layout_web_progress_view, this, true)
        webview = rootView.findViewById(R.id.my_web_view)
        bridgeHelper = createBridgeHelper()
        webview.webChromeClient = MyWebChromeClient()
        webview.webViewClient = MyWebViewClient()
        webview.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
        }
    }

    private inner class MyWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            if (title.contains("html")) {
                return
            }
            listener?.onTitle(title)
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            filePathCallback ?: return false
            fileChooserCallback?.onReceiveValue(null)
            fileChooserCallback = filePathCallback
            val activity = context.findFragmentActivity()
            val params = fileChooserParams
            if (activity == null || params == null) {
                deliverFileChooserResult(emptyList())
                return false
            }
            activity.lifecycleScope.launch {
                val uris = runCatching {
                    UploadPickerHostFragment.obtain(activity).pickWebFiles(
                        acceptTypes = params.acceptTypes ?: emptyArray(),
                        allowMultiple = params.mode == FileChooserParams.MODE_OPEN_MULTIPLE
                    )
                }.getOrElse { throwable ->
                    Log.w(TAG, "file chooser failed", throwable)
                    emptyList()
                }
                deliverFileChooserResult(uris)
            }
            return true
        }
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
            super.onPageStarted(view, url, favicon)
            prepareForPageLoad()
        }

        @Deprecated(
            message = "Compatibility override for legacy WebView callback",
            replaceWith = ReplaceWith("shouldOverrideUrlLoading(view, request)")
        )
        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return bridgeHelper.shouldOverrideUrlLoading(url) || super.shouldOverrideUrlLoading(view, url)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            val url = request.url?.toString().orEmpty()
            if (url.isNotEmpty() && bridgeHelper.shouldOverrideUrlLoading(url)) {
                return true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            bridgeHelper.onPageFinished()
            //在访问失败的时候会首先回调onReceivedError，然后再回调onPageFinished。
            if (!isError) {
                isLastLoadSuccess = true
                listener?.success()
            }
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            if (!request.isForMainFrame) {
                return
            }
            notifyMainFrameError()
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            if (!request.isForMainFrame || errorResponse.statusCode < 400) {
                return
            }
            notifyMainFrameError()
        }

        private fun notifyMainFrameError() {
            //在访问失败的时候会首先回调onReceivedError，然后再回调onPageFinished。
            isError = true
            if (!isLastLoadSuccess) {//之前成功加载完成过，不会回调
                listener?.error()
            }
        }
    }

    /**
     * 千万不要更改这个 "SSDJsBirdge"  注意！！！！！
     */
    @SuppressLint("JavascriptInterface")
    fun addJavascriptInterface(jsInterface: Any, interfaceName: String = DEFAULT_JS_INTERFACE_NAME) {
        webview.addJavascriptInterface(jsInterface, interfaceName)
    }

    fun reload() {
        prepareForPageLoad()
        webview.reload()
    }

    fun loadUrl(url: String) {
        prepareForPageLoad()
        try {
            webview.loadUrl(url)
        } catch (e: Exception) {
            Log.w(TAG, "loadUrl failed, url=$url", e)
        }

    }

    fun callHandler(
        handlerName: String,
        data: String? = null,
        callback: ((String) -> Unit)? = null
    ) {
        bridgeHelper.callHandler(handlerName, data, callback.asBridgeCallback())
    }

    fun callHandlerPersistent(
        handlerName: String,
        data: String? = null,
        callback: ((String) -> Unit)? = null
    ) {
        bridgeHelper.callHandler(handlerName, data, callback.asBridgeCallback())
    }

    fun sendToWeb(
        data: String,
        callback: ((String) -> Unit)? = null
    ) {
        bridgeHelper.sendToWeb(data, callback.asBridgeCallback())
    }

    fun setDefaultHandler(handler: (String?, ((String) -> Unit)?) -> Unit) {
        defaultBridgeHandler = handler
        bridgeHelper.setDefaultHandler(handler.asBridgeHandler())
    }

    fun registerHandler(
        handlerName: String,
        handler: (String?, ((String) -> Unit)?) -> Unit
    ) {
        bridgeHandlers[handlerName] = handler
        bridgeHelper.registerHandler(handlerName, handler.asBridgeHandler())
    }

    fun unregisterHandler(handlerName: String) {
        bridgeHandlers.remove(handlerName)
        bridgeHelper.unregisterHandler(handlerName)
    }

    fun removeJavascriptInterface(interfaceName: String = DEFAULT_JS_INTERFACE_NAME) {
        webview.removeJavascriptInterface(interfaceName)
    }

    fun canGoBack(): Boolean {
        return webview.canGoBack()
    }

    fun goBackIfPossible(): Boolean {
        if (!webview.canGoBack()) {
            return false
        }
        webview.goBack()
        return true
    }

    fun currentUrl(): String? {
        return webview.url
    }

    /**
     * must be called on the main thread
     */
    fun destory() {
        try {
            bridgeHandlers.clear()
            defaultBridgeHandler = null
            fileChooserCallback?.onReceiveValue(null)
            fileChooserCallback = null
            webview.stopLoading()
            webview.webChromeClient = null
            webview.webViewClient = WebViewClient()
            webview.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "destroy webview failed", e)
        }

    }

    private fun prepareForPageLoad() {
        bridgeHelper = createBridgeHelper()
        resetLoadState()
    }

    private fun resetLoadState() {
        isError = false
        isLastLoadSuccess = false
    }

    private fun createBridgeHelper(): BridgeHelper {
        val helper = BridgeHelper(object : IWebView {
            override fun getContext(): Context = webview.context

            override fun loadUrl(url: String) {
                webview.loadUrl(url)
            }
        })
        defaultBridgeHandler?.let { handler ->
            helper.setDefaultHandler(handler.asBridgeHandler())
        }
        bridgeHandlers.forEach { (handlerName, handler) ->
            helper.registerHandler(handlerName, handler.asBridgeHandler())
        }
        return helper
    }

    private fun deliverFileChooserResult(uris: List<Uri>) {
        val callback = fileChooserCallback ?: return
        fileChooserCallback = null
        callback.onReceiveValue(
            uris.takeIf(List<Uri>::isNotEmpty)
                ?.toTypedArray()
        )
    }

    private fun ((String) -> Unit)?.asBridgeCallback(): OnBridgeCallback? {
        return this?.let { block ->
            OnBridgeCallback { response -> block(response) }
        }
    }

    private fun ((String?, ((String) -> Unit)?) -> Unit).asBridgeHandler(): BridgeHandler {
        return BridgeHandler { data, callback ->
            this(data, callback.asBridgeLambda())
        }
    }

    private fun OnBridgeCallback?.asBridgeLambda(): ((String) -> Unit)? {
        return this?.let { callback ->
            { response -> callback.onCallBack(response) }
        }
    }

    private var listener: OnWebLoadStatusListener? = null

    fun setOnLoadStatueListener(listener: OnWebLoadStatusListener) {
        this.listener = listener
    }

    interface OnWebLoadStatusListener {
        fun error()

        fun success()

        fun onTitle(title: String)
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) {
            return current
        }
        current = current.baseContext
    }
    return null
}
