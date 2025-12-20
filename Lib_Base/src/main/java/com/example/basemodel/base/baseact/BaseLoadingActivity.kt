package com.example.basemodel.base.baseact

import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.lib.base.R


/**
 * @author 浩楠
 * @date 2025/5/22 14:44
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 添加 Loading Dialog 支持
 */
abstract class BaseLoadingActivity<V : ViewBinding, VM : BaseViewModel> :
    BaseMVVMActivity<V, VM>() {

    private var dialog: MaterialDialog? = null

    override fun registerUIObservers() {
        super.registerUIObservers()

        mViewModel.uc.getShowDialog().observe(this) { showLoading() }
        mViewModel.uc.getDismissDialog().observe(this) { dismissLoading() }
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
}
