package com.example.basemodel.base.baseact

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.example.basemodel.base.baseint.IBaseView
import com.example.basemodel.base.basevm.BaseViewModel
import com.example.basemodel.base.basevm.BaseViewModel.Companion.ParameterField.BUNDLE
import com.example.basemodel.base.basevm.BaseViewModel.Companion.ParameterField.CANONICAL_NAME
import com.example.basemodel.base.basevm.BaseViewModel.Companion.ParameterField.CLASS
import com.example.basemodel.base.basevm.BaseViewModel.Companion.ParameterField.REQUEST
import com.ghn.lib.base.R
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kairowan.lib_ui_common.ext.showToast

abstract class BaseActivity<V : ViewBinding, VM : BaseViewModel> :
    BaseCoreActivity<V, VM>(), IBaseView {

    private var commonTitleBar: TitleBar? = null
    private var commonContentContainer: FrameLayout? = null
    private var dialog: MaterialDialog? = null
    private var pendingRequestCode: Int? = null
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val requestCode = pendingRequestCode ?: BaseViewModel.Companion.ParameterField.REQEUST_DEFAULT
        pendingRequestCode = null
        mViewModel.onActivityResult(requestCode, result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BaseCoreActivity>.onCreate(savedInstanceState)
        registerUIObservers()
        lifecycle.addObserver(this)
    }

    override fun wrapContentView(contentView: View): View {
        if (!useCommonTitleBar()) {
            return contentView
        }
        val rootView = LayoutInflater.from(this)
            .inflate(R.layout.layout_base_title_shell, null, false)
        commonTitleBar = rootView.findViewById(R.id.base_common_title_bar)
        commonContentContainer = rootView.findViewById(R.id.base_content_container)
        commonContentContainer?.addView(
            contentView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        applyCommonTitleBarConfig()
        return rootView
    }

    protected open fun useCommonTitleBar(): Boolean = false

    protected open fun commonTitleBarTitle(): CharSequence = ""

    @ColorInt
    protected open fun commonTitleBarBackgroundColor(): Int = Color.WHITE

    @ColorInt
    protected open fun commonTitleBarTitleColor(): Int = Color.BLACK

    @ColorInt
    protected open fun commonTitleBarLeftTitleColor(): Int = commonTitleBarTitleColor()

    @ColorInt
    protected open fun commonTitleBarRightTitleColor(): Int = commonTitleBarTitleColor()

    @DrawableRes
    protected open fun commonTitleBarLeftIconRes(): Int? = null

    @DrawableRes
    protected open fun commonTitleBarRightIconRes(): Int? = null

    protected open fun commonTitleBarRightTitle(): CharSequence = ""

    protected open fun commonTitleBarShowLine(): Boolean = true

    @ColorInt
    protected open fun commonStatusBarColor(): Int = commonTitleBarBackgroundColor()

    protected open fun commonUseDarkStatusBarIcons(): Boolean = true

    @ColorInt
    protected open fun commonNavigationBarColor(): Int? = null

    protected open fun commonUseDarkNavigationBarIcons(): Boolean = commonUseDarkStatusBarIcons()

    protected open fun onCommonTitleBarLeftClick() {
        finish()
    }

    protected open fun onCommonTitleBarTitleClick() = Unit

    protected open fun onCommonTitleBarRightClick() = Unit

    protected open fun onBindCommonTitleBar(titleBar: TitleBar) = Unit

    protected fun setCommonTitleBarTitle(title: CharSequence) {
        commonTitleBar?.setTitle(title)
    }

    protected fun setCommonTitleBarVisible(visible: Boolean) {
        commonTitleBar?.isVisible = visible
    }

    protected fun getCommonTitleBar(): TitleBar? = commonTitleBar

    private fun applyCommonTitleBarConfig() {
        val titleBar = commonTitleBar ?: return
        titleBar.isVisible = true
        titleBar.setBackgroundColor(commonTitleBarBackgroundColor())
        titleBar.setTitle(commonTitleBarTitle())
        titleBar.setTitleColor(commonTitleBarTitleColor())
        titleBar.setLeftTitleColor(commonTitleBarLeftTitleColor())
        titleBar.setRightTitleColor(commonTitleBarRightTitleColor())
        titleBar.setLineVisible(commonTitleBarShowLine())
        titleBar.setRightTitle(commonTitleBarRightTitle())
        commonTitleBarLeftIconRes()?.let { iconRes ->
            titleBar.setLeftIcon(iconRes)
        } ?: titleBar.setLeftIcon(defaultCommonTitleBarLeftIconRes())
        commonTitleBarRightIconRes()?.let { iconRes ->
            titleBar.setRightIcon(iconRes)
        }
        titleBar.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(titleBar: TitleBar) {
                onCommonTitleBarLeftClick()
            }

            override fun onTitleClick(titleBar: TitleBar) {
                onCommonTitleBarTitleClick()
            }

            override fun onRightClick(titleBar: TitleBar) {
                onCommonTitleBarRightClick()
            }
        })
        onBindCommonTitleBar(titleBar)
        applyCommonSystemBarStyle()
    }

    @DrawableRes
    private fun defaultCommonTitleBarLeftIconRes(): Int {
        return if (commonUseDarkStatusBarIcons()) {
            com.hjq.bar.R.drawable.bar_arrows_left_black
        } else {
            com.hjq.bar.R.drawable.bar_arrows_left_white
        }
    }

    private fun applyCommonSystemBarStyle() {
        window.statusBarColor = commonStatusBarColor()
        commonNavigationBarColor()?.let { navigationBarColor ->
            window.navigationBarColor = navigationBarColor
        }
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = commonUseDarkStatusBarIcons()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            controller.isAppearanceLightNavigationBars = commonUseDarkNavigationBarIcons()
        }
    }

    open fun registerUIObservers() {
        mViewModel.uc.getStartActivityEvent().observe(this) { params ->
            params?.let {
                val clz = it[CLASS] as? Class<*>
                if (clz == null) return@observe
                val intent = Intent(this, clz)
                val bundle = it[BUNDLE] as? Bundle
                bundle?.let { intent.putExtras(it) }
                val requestCode = it[REQUEST] as? Int
                if (requestCode != null) {
                    pendingRequestCode = requestCode
                    activityResultLauncher.launch(intent)
                } else {
                    startActivity(intent)
                }
            }
        }

        mViewModel.uc.getStartModelActivityEvent().observe(this) {
            val classValue = it?.get(CLASS)
            val pkg = it?.get(CANONICAL_NAME)?.toString()
            val className = when (classValue) {
                is Class<*> -> classValue.name
                else -> classValue?.toString()
            }
            if (!pkg.isNullOrBlank() && !className.isNullOrBlank()) {
                val intent = Intent().apply {
                    setClassName(pkg, className)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }

        mViewModel.uc.getFinishEvent().observe(this) { finish() }

        mViewModel.uc.getOnBackPressedEvent().observe(this) { onBackPressedDispatcher.onBackPressed() }

        mViewModel.uc.getSetResultEvent().observe(this) { result ->
            val intent = Intent()
            result?.forEach { intent.putExtra(it.key, it.value.toString()) }
            setResult(RESULT_OK, intent)
        }

        mViewModel.uc.getFinishResult().observe(this) {
            setResult(it!!)
            finish()
        }

        mViewModel.uc.getShowDialog().observe(this) { showLoading() }
        mViewModel.uc.getDismissDialog().observe(this) { dismissLoading() }

        mViewModel.uc.toastEvent().observe(this) { showMsg(it.toString()) }
    }

    protected fun showLoading() {
        if (dialog == null) {
            dialog = MaterialDialog(this)
                .cancelOnTouchOutside(false)
                .cornerRadius(8f)
                .customView(R.layout.custom_progress_dialog_view, noVerticalPadding = true)
                .lifecycleOwner(this)
                .maxWidth(R.dimen.dialog_width)
        }
        dialog?.show()
    }

    protected fun dismissLoading() {
        dialog?.takeIf { it.isShowing }?.dismiss()
    }

    protected fun showMsg(msg: String) {
        showToast(msg)
    }

    protected fun showMsgWithImage(msg: String, iconRes: Int) {
        showToast(msg, iconRes = iconRes)
    }
}
