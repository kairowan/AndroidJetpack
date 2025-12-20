package com.example.basemodel.base.basevm

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.trello.rxlifecycle4.LifecycleProvider
import java.lang.ref.WeakReference

/**
 * @author 浩楠
 *
 * @date 2023/6/4-14:28
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO 封装我们的BaseModel然后，通过观察者模式对数据进行观察并且通知我们的UI进行更新数据
 */
open class BaseViewModel(application: Application) :
    AndroidViewModel(application), BaseViewModelScope,
    BaseViewModelNavigation,
    BaseViewModelLifecycle,
    BaseViewModelLiveData,
    BaseViewModelLauncher {

    private var mLifecycle: WeakReference<LifecycleProvider<*>>? = null
//    private var uc: UIChangeLiveData? = null
//    private lateinit var context: Application

    /**
     * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动
     * 调用ViewModel的  #onCleared 方法取消所有协程
     */
//    @OptIn(DelicateCoroutinesApi::class)
//    fun launchUI(block: suspend CoroutineScope.() -> Unit) {
//        //网络可用
//
//        GlobalScope.launch(Dispatchers.IO) {
//            viewModelScope.launch { block() }
//        }
//
//    }

//    override fun onAny(owner: LifecycleOwner?, event: Lifecycle.Event?) {
//
//    }
//
//    override fun onCreate() {
//
//
//    }
//
//    override fun onDestroy() {
//    }
//
//    override fun onStart() {
//    }
//
//    override fun onStop() {
//    }
//
//    override fun onResume() {
//    }
//
//    override fun onPause() {
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//    }

    /**
     *  不过滤请求结果
     * @param block 请求体
     * @param error 失败回调
     * @param complete  完成回调（无论成功失败都会调用）
     * @param isShowDialog 是否显示加载框
     */
//    fun launchGo(
//        block: suspend CoroutineScope.() -> Unit,
//        error: suspend CoroutineScope.(ResponseThrowable) -> Unit = {
//            uc?.toastEvent()?.postValue("${it.code}:${it.errMsg}")
//        },
//        complete: suspend CoroutineScope.() -> Unit = {},
//        isShowDialog: Boolean = true
//    ) {
//        if (isShowDialog) uc?.getShowDialog()?.call()
//        launchUI {
//            handleException(
//                withContext(Dispatchers.IO) { block },
//                { error(it) },
//                {
//                    uc?.getDismissDialog()?.call()
//                    complete()
//                }
//            )
//        }
//    }

    /**
     * 过滤请求结果，其他全抛异常
     * @param block 请求体
     * @param success 成功回调
     * @param error 失败回调
     * @param complete  完成回调（无论成功失败都会调用）
     * @param isShowDialog 是否显示加载框
     */
//    fun <T> launchOnlyresult(
//        block: suspend CoroutineScope.() -> BaseResult<T>,
//        success: (T?) -> Unit,
//
//        error: (ResponseThrowable) -> Unit = {
//            it.printStackTrace()
//        },
//        complete: () -> Any = {},
//        isShowDialog: Boolean = true
//    ) {
//        if (isShowDialog) uc?.getShowDialog()?.call()
//        launchUI {
//            handleException({
//                withContext(Dispatchers.IO) {
//                    block().let {
//                        if (it.isSuccess()) {
//                            it.data()
//                        } else {
//                            uc?.toastEvent()?.postValue(it.errorMsg())
//                            throw ResponseThrowable(it.errorCode(), it.errorMsg())
//                        }
//                    }
//
//                }.also {
//                    success(it)
//
//                }
////                startModelActivity("com.ghn.cocknovel","com.ghn.cocknovel.ui.activity.SwitchActivity")
//
//            }, {
//                error(it)
//            }, {
//                uc?.getDismissDialog()?.call()
//                complete()
//
//            })
//        }
//    }

    /**
     * flow 运行在主线程中，可直接调用
     * @param errorCall 错误回调
     * @param requestCall 请求函数
     * @param isShowDialog 是否展示加载框
     * @param successBlock 请求结果
     */
//    fun <T> launchFlow(
//        requestCall: suspend () -> BaseResult<T>?,
//        successBlock: (T?) -> Unit,
//        isShowDialog: Boolean = true,
//        errorCall: (ResponseThrowable) -> Unit = {
//            it.printStackTrace()
//        }
//    ) {
//        if (isShowDialog) uc?.getShowDialog()?.call()
//        viewModelScope.launch(Dispatchers.Main) {
//            val data = requestFlow(errorBlock = { code, error ->
//                errorCall(error!!)
//            }, requestCall)
//            successBlock(data)
//        }
//    }

    /**
     * 异常统一处理
     */
//    private suspend fun handleException(
//        block: suspend CoroutineScope.() -> Unit,
//        error: suspend CoroutineScope.(ResponseThrowable) -> Unit,
//        complete: suspend CoroutineScope.() -> Unit
//    ) {
//        coroutineScope {
//            try {
//                block()
//            } catch (e: Throwable) {
//                error(ExceptionHandle.handleException(e))
//            } finally {
//                complete()
//            }
//        }
//    }


    /**
     * 注入RxLifecycle生命周期
     *
     * @param lifecycle
     */
    fun injectLifecycleProvider(lifecycle: LifecycleProvider<*>?) {
        mLifecycle = WeakReference(lifecycle)

    }


//    /**
//     * 跳转页面
//     *
//     * @param clz 所跳转的目的Activity类
//     */
//    fun startActivity(clz: Class<out Activity?>?) {
//        startActivity(clz, null)
//    }
//
//    /**
//     * @param clz  clz 所跳转的目的Activity类
//     * @param code 启动requestCode
//     */
//    fun startActivity(clz: Class<out Activity?>?, code: Int) {
//        startActivity(clz, null, code)
//    }

//    /**
//     * 跳转页面
//     *
//     * @param clz    所跳转的目的Activity类
//     * @param bundle 跳转所携带的信息
//     */
//    private fun startActivity(clz: Class<out Activity?>?, bundle: Bundle?) {
//        startActivity(clz, bundle, REQEUST_DEFAULT)
//    }
//
//    /**
//     * 跳转页面
//     *
//     * @param clz    所跳转的目的Activity类
//     * @param bundle 跳转所携带的信息
//     */
//    fun startActivity(clz: Class<out Activity?>?, bundle: Bundle?, requestCode: Int) {
//        val params: MutableMap<String, Any?> = HashMap()
//        params[CLASS] = clz
//        params[REQUEST] = requestCode
//        params[BUNDLE] = bundle
//        uc?.getStartActivityEvent()?.postValue(params as Map<String, Any>)
//    }
//
//    /**
//     * 从model中跳转到主app中的activity
//     *
//     * @param clz 所跳转的目的Activity类
//     */
//    fun startModelActivity(Packagename: String?, clz: String?) {
//        val params: MutableMap<String, Any?> = HashMap()
//        params[CLASS] = clz
//        params[CANONICAL_NAME] = Packagename
//        uc?.getStartModelActivityEvent()?.postValue(params as Map<String, Any>)
//    }
//
//
//    fun startActivityForFragment(clz: Class<out Activity?>, bundle: Bundle, requestCode: Int) {
//        val params: MutableMap<String, Any> = HashMap()
//        params[CLASS] = clz
//        params[REQUEST] = requestCode
//        params[BUNDLE] = bundle
//        uc?.getStartActivityForFragment()?.postValue(params)
//    }
//
//    /**
//     * 关闭界面
//     */
//    fun finish() {
//        uc?.getFinishEvent()?.postValue(null)
//    }
//
//    /**
//     * 携带code的 finish
//     */
//    fun finishFragmentResult() {
//        uc?.getResultFragment()?.postValue(null)
//    }
//
//    /**
//     * 返回上一层
//     */
//    fun onBackPressed() {
//        uc?.getOnBackPressedEvent()!!.postValue(null)
//    }
//
//    fun getUC(): UIChangeLiveData? {
//        if (uc == null) {
//            uc = UIChangeLiveData()
//        }
//        return uc
//    }

    override val uc: UIChangeLiveData by lazy { UIChangeLiveData() }
    companion object {
        object ParameterField {
            const val CLASS = "CLASS"
            const val CANONICAL_NAME = "CANONICAL_NAME"
            const val BUNDLE = "BUNDLE"
            const val REQUEST = "REQUEST"
            const val REQEUST_DEFAULT = 1
        }
    }

    override fun onAny(owner: LifecycleOwner?, event: Lifecycle.Event?) {

    }

    override fun onCreate() {

    }

    override fun onDestroy() {

    }

    override fun onStart() {

    }

    override fun onStop() {

    }

    override fun onResume() {

    }

    override fun onPause() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

}