package com.example.basemodel.base.baseact

import android.content.Intent
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.example.basemodel.base.basevm.BaseViewModel
import com.example.basemodel.base.basevm.BaseViewModel.Companion.ParameterField.BUNDLE
import com.example.basemodel.base.basevm.BaseViewModel.Companion.ParameterField.CANONICAL_NAME
import com.example.basemodel.base.basevm.BaseViewModel.Companion.ParameterField.CLASS
import com.example.basemodel.base.basevm.BaseViewModel.Companion.ParameterField.REQUEST


/**
 * @author 浩楠
 * @date 2025/5/22 14:42
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 添加通用 LiveData 页面跳转、关闭、setResult 事件等
 */
abstract class BaseMVVMActivity<V : ViewBinding, VM : BaseViewModel> :
    BaseCoreActivity<V, VM>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerUIObservers()
    }

    open fun registerUIObservers() {
        mViewModel.uc.getStartActivityEvent().observe(this) { params ->
            params?.let {
                val clz = it[CLASS] as? Class<*>
                val intent = Intent(this, clz)
                val bundle = it[BUNDLE] as? Bundle
                bundle?.let { intent.putExtras(it) }
                startActivityForResult(intent, it[REQUEST] as Int)
            }
        }

        mViewModel.uc.getStartModelActivityEvent().observe(this) {
            val clz = it!![CLASS]?.toString()
            val pkg = it[CANONICAL_NAME]?.toString()
            val intent = Intent().apply {
                setClassName(pkg!!, clz!!)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        mViewModel.uc.getFinishEvent().observe(this) { finish() }

        mViewModel.uc.getOnBackPressedEvent().observe(this) { onBackPressed() }

        mViewModel.uc.getSetResultEvent().observe(this) { result ->
            val intent = Intent()
            result?.forEach { intent.putExtra(it.key, it.value.toString()) }
            setResult(RESULT_OK, intent)
        }

        mViewModel.uc.getFinishResult().observe(this) {
            setResult(it!!)
            finish()
        }
    }
}
