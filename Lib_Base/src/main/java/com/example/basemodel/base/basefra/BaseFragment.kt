package com.example.basemodel.base.basefra

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.lib.base.R
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.kairowan.lib_ui_common.ext.showToast

abstract class BaseFragment<V : ViewBinding, VM : BaseViewModel> :
    BaseCoreFragment<V, VM>() {

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

    override fun wrapContentView(
        contentView: View,
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View {
        if (!useCommonTitleBar()) {
            return contentView
        }
        val rootView = inflater.inflate(R.layout.layout_base_title_shell, container, false)
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

    override fun initViewObservable() {
        registerUIObservers()
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
        activity?.onBackPressedDispatcher?.onBackPressed()
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
    }

    override fun onResume() {
        super.onResume()
        if (useCommonTitleBar()) {
            applyCommonSystemBarStyle()
        }
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
        val activityWindow = activity?.window ?: return
        activityWindow.statusBarColor = commonStatusBarColor()
        commonNavigationBarColor()?.let { navigationBarColor ->
            activityWindow.navigationBarColor = navigationBarColor
        }
        val controller = WindowInsetsControllerCompat(activityWindow, activityWindow.decorView)
        controller.isAppearanceLightStatusBars = commonUseDarkStatusBarIcons()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            controller.isAppearanceLightNavigationBars = commonUseDarkNavigationBarIcons()
        }
    }

    open fun registerUIObservers() {
        mViewModel.uc.getStartActivityEvent()?.observe(viewLifecycleOwner) { params ->
            params?.let {
                val clz = params[BaseViewModel.Companion.ParameterField.CLASS] as? Class<*>
                if (clz == null) return@observe
                val intent = Intent(activity, clz)
                val bundle = params[BaseViewModel.Companion.ParameterField.BUNDLE]
                if (bundle is Bundle) {
                    intent.putExtras((bundle as Bundle?)!!)
                }
                val requestCode = params[BaseViewModel.Companion.ParameterField.REQUEST] as? Int
                if (requestCode != null) {
                    pendingRequestCode = requestCode
                    activityResultLauncher.launch(intent)
                } else {
                    this@BaseFragment.startActivity(intent)
                }
            }
        }

        mViewModel.uc.getStartModelActivityEvent()?.observe(viewLifecycleOwner) { params ->
            val classValue = params?.get(BaseViewModel.Companion.ParameterField.CLASS)
            val pkg = params?.get(BaseViewModel.Companion.ParameterField.CANONICAL_NAME)?.toString()
            val className = when (classValue) {
                is Class<*> -> classValue.name
                else -> classValue?.toString()
            }
            if (pkg.isNullOrBlank() || className.isNullOrBlank()) return@observe
            val intent = Intent()
            intent.setClassName(pkg, className)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this@BaseFragment.startActivity(intent)
        }

        mViewModel.uc.getFinishEvent()?.observe(viewLifecycleOwner) {
            activity?.finish()
        }

        mViewModel.uc.getOnBackPressedEvent()?.observe(viewLifecycleOwner) {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        mViewModel.uc.getSetResultEvent()?.observe(viewLifecycleOwner) { result ->
            val intent = Intent()
            result?.forEach { intent.putExtra(it.key, it.value.toString()) }
            activity?.setResult(AppCompatActivity.RESULT_OK, intent)
        }

        mViewModel.uc.getFinishResult()?.observe(viewLifecycleOwner) {
            activity?.setResult(it!!)
            activity?.finish()
        }

        mViewModel.uc.getShowDialog().observe(viewLifecycleOwner) { showLoading() }
        mViewModel.uc.getDismissDialog().observe(viewLifecycleOwner) { dismissLoading() }

        mViewModel.uc.toastEvent().observe(viewLifecycleOwner) { message ->
            showMsg(message.toString())
        }
    }

    protected fun showLoading() {
        if (dialog == null) {
            dialog = context?.let {
                MaterialDialog(it)
                    .cancelable(false)
                    .cornerRadius(8f)
                    .customView(R.layout.custom_progress_dialog_view, noVerticalPadding = true)
                    .lifecycleOwner(this)
                    .maxWidth(R.dimen.dialog_width)
            }
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
