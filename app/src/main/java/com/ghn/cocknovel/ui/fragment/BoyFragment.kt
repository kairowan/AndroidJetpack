package com.ghn.cocknovel.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basemodel.base.basefra.BaseFragment
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.databinding.FragmentBoyBinding
import com.ghn.cocknovel.viewmodel.RecommendViewModel
import com.ghn.routermodule.AppRouter
import com.ghn.routermodule.RouterPath


class BoyFragment : BaseFragment<FragmentBoyBinding, RecommendViewModel>(){
//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBoyBinding = FragmentBoyBinding.inflate(inflater,container,false)


    override fun initParam() {

    }

    override fun initView() {
        mBinding.TvNav.setOnClickListener {
            AppRouter.goTo(RouterPath.Main.HOME)
        }
        mBinding.TvNavElse.setOnClickListener {
           AppRouter.geToKey()
        }
        mBinding.TvNavKey.setOnClickListener {
            AppRouter.goToProfile("https://www.baidu.com/")
        }

    }

    override fun initData() {
    }

    override fun initViewObservable() {
    }
}