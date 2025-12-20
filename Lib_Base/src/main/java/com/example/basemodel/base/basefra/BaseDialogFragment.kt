package com.example.basemodel.base.basefra

import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.lib.base.R


/**
 * @author 浩楠
 * @date 2025/5/22 15:43
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 通用 Dialog loading 显示/隐藏逻辑
 */
abstract class BaseDialogFragment<V : ViewBinding, VM : BaseViewModel> :
    BaseMVVMFragment<V, VM>() {

    private var dialog: MaterialDialog? = null

    override fun initViewObservable() {
        super.initViewObservable()

        mViewModel.uc.getShowDialog().observe(this) {
            showLoading()
        }

        mViewModel.uc.getDismissDialog().observe(this) {
            dismissLoading()
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
}
