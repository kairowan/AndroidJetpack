package com.example.basemodel.base.baseact

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.basemodel.base.basevm.BaseViewModel
import com.therouter.TheRouter
import org.koin.androidx.viewmodel.ext.android.viewModelForClass
import java.lang.reflect.ParameterizedType

abstract class BaseCoreActivity<V : ViewBinding, VM : BaseViewModel> :
    AppCompatActivity() {

    protected lateinit var mBinding: V
    protected lateinit var mViewModel: VM

    abstract fun initContentView(savedInstanceState: Bundle?): V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = initContentView(savedInstanceState)
        setContentView(mBinding.root)

        TheRouter.inject(this)
        initViewModel()
        lifecycle.addObserver(mViewModel)
    }

    @Suppress("UNCHECKED_CAST")
    private fun initViewModel() {
        val modelClass = (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[1] as Class<VM>
        mViewModel = viewModelForClass(modelClass.kotlin).value
    }
}
