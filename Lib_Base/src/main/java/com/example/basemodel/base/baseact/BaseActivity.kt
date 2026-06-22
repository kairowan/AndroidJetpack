package com.example.basemodel.base.baseact

import android.content.Intent
import android.os.Bundle
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
import com.kairowan.lib_ui_common.ext.showToast

abstract class BaseActivity<V : ViewBinding, VM : BaseViewModel> :
    BaseCoreActivity<V, VM>(), IBaseView {

    private var dialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<BaseCoreActivity>.onCreate(savedInstanceState)
        registerUIObservers()
        lifecycle.addObserver(this)
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
                    startActivityForResult(intent, requestCode)
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
