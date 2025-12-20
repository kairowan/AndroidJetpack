package com.example.basemodel.base.baseact

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.example.basemodel.base.basevm.BaseViewModel
import com.therouter.TheRouter
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import java.lang.reflect.ParameterizedType

/**
 * @author 浩楠
 * @date 2025/5/22 14:40
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 最底层基类 绑定 ViewModel + ViewBinding
 */
abstract class BaseCoreActivity<V : ViewBinding, VM : BaseViewModel> :
    RxAppCompatActivity(), LifecycleObserver {

    protected lateinit var mBinding: V
    protected lateinit var mViewModel: VM

//    abstract fun initVariableId(): Int
    abstract fun initContentView(savedInstanceState: Bundle?): V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = initContentView(savedInstanceState)
        setContentView(mBinding.root)

        TheRouter.inject(this)
        initViewModel()
        lifecycle.addObserver(mViewModel)
    }

    private fun initViewModel() {
        val modelClass = (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[1] as Class<VM>
        mViewModel = ViewModelProvider(this)[modelClass]
        mViewModel.injectLifecycleProvider(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
