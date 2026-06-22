package com.example.basemodel.base.basefra

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.lib.base.R
import com.kairowan.lib_ui_common.ext.showToast

abstract class BaseFragment<V : ViewBinding, VM : BaseViewModel> :
    BaseCoreFragment<V, VM>() {

    private var dialog: MaterialDialog? = null

    override fun initViewObservable() {
        registerUIObservers()
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
                    this@BaseFragment.startActivityForResult(intent, requestCode)
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
