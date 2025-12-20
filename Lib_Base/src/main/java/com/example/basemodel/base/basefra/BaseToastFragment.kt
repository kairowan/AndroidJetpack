package com.example.basemodel.base.basefra

import androidx.viewbinding.ViewBinding
import com.example.basemodel.base.basevm.BaseViewModel
import com.kairowan.lib_ui_common.ext.showToast


/**
 * @author 浩楠
 * @date 2025/5/22 15:44
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO
 */
abstract class BaseToastFragment<V : ViewBinding, VM : BaseViewModel> :
    BaseDialogFragment<V, VM>() {

    override fun initViewObservable() {
        super.initViewObservable()

        mViewModel.uc.toastEvent().observe(this) { message ->
            showMsg(message.toString())
        }
    }

    protected fun showMsg(msg: String) {
        showToast(msg)
    }

    protected fun showMsgWithImage(msg: String, iconRes: Int) {
        showToast(msg,iconRes=iconRes)
    }
}
