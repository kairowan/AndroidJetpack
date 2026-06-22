package com.example.basemodel.base.basefra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.example.basemodel.base.basevm.BaseViewModel
import com.example.basemodel.base.baseint.IBaseView
import org.koin.androidx.viewmodel.ext.android.viewModelForClass
import java.lang.reflect.ParameterizedType

abstract class BaseCoreFragment<V : ViewBinding, VM : BaseViewModel> :
    Fragment(), IBaseView {

    protected lateinit var mBinding: V
    protected lateinit var mViewModel: VM

    open var viewModelId: Int = 0
    private var isFirst: Boolean = true

    abstract fun initContentView(inflater: LayoutInflater, container: ViewGroup?): V

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = initContentView(inflater, container)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViewObservable()
        lifecycle.addObserver(this)

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (isFirst) {
                    isFirst = false
                    lazyLoadData()
                }
            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    private fun initViewModel() {
        val modelClass = (javaClass.genericSuperclass as? ParameterizedType)
            ?.actualTypeArguments?.get(1) as? Class<VM>
            ?: throw IllegalStateException("Unable to resolve ViewModel type for ${javaClass.simpleName}. Ensure the class extends BaseFragment<XBinding, YourViewModel>.")
        mViewModel = viewModelForClass(modelClass.kotlin).value
    }

    open fun lazyLoadData() {}
}
