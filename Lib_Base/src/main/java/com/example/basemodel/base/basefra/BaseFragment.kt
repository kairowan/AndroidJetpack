package com.example.basemodel.base.basefra

import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.example.basemodel.base.basevm.BaseViewModel

/**
 * @author 浩楠
 *
 * @date 2023/6/4-17:23
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO 最终对外暴露的基类
 */
abstract class BaseFragment<V : ViewBinding, VM : BaseViewModel> :
    BaseToastFragment<V, VM>() {

//    private var isFirstLoad = true

//    override fun onResume() {
//        super.onResume()
//        if (isFirstLoad) {
//            lazyLoadData()
//            isFirstLoad = false
//        }
//    }
//
//    /**
//     * 首次可见时懒加载，子类可重写
//     */
//    override fun lazyLoadData() {}

    override fun initViewObservable() {
        super.initViewObservable()

    }
}
