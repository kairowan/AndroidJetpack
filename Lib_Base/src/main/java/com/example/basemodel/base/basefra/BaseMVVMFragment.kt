package com.example.basemodel.base.basefra

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.example.basemodel.base.basevm.BaseViewModel

/**
 * @author 浩楠
 * @date 2025/5/22 15:38
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 添加通用 LiveData 页面跳转、关闭、setResult 事件等
 */
abstract class BaseMVVMFragment<V : ViewBinding, VM : BaseViewModel> :
    BaseCoreFragment<V, VM>() {


    override fun initViewObservable() {
        registerUIObservers()
    }
    open fun registerUIObservers() {
        mViewModel.uc.getStartActivityEvent()?.observe(this) { params ->
            params?.let {
                val clz = params[BaseViewModel.Companion.ParameterField.CLASS] as Class<*>?
                val intent = Intent(activity, clz)
                val bundle = params[BaseViewModel.Companion.ParameterField.BUNDLE]
                if (bundle is Bundle) {
                    intent.putExtras((bundle as Bundle?)!!)
                }
                this@BaseMVVMFragment.startActivityForResult(
                    intent,
                    params[BaseViewModel.Companion.ParameterField.REQUEST] as Int
                )
            }
        }

        mViewModel.uc.getStartModelActivityEvent()?.observe(this) {params ->

            val clz = params?.get(BaseViewModel.Companion.ParameterField.CLASS) as Class<*>?
            val pkg = params?.get(BaseViewModel.Companion.ParameterField.CANONICAL_NAME)
            val intent=Intent()
            intent.setClassName(pkg.toString(), clz.toString())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this@BaseMVVMFragment.startActivity(intent)
        }

        mViewModel.uc.getFinishEvent()?.observe(this) {
            activity?.finish()
        }

        mViewModel.uc.getOnBackPressedEvent()?.observe(this) {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        mViewModel.uc.getSetResultEvent()?.observe(this) { result ->
            val intent = Intent()
            result?.forEach { intent.putExtra(it.key, it.value.toString()) }
            activity?.setResult(AppCompatActivity.RESULT_OK, intent)
        }

        mViewModel.uc.getFinishResult()?.observe(this) {
            activity?.setResult(it!!)
            activity?.finish()
        }
    }
}
