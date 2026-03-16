package com.ghn.cocknovel.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basemodel.base.basefra.BaseFragment
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.databinding.FragmentBoyBinding
import com.ghn.cocknovel.viewmodel.RecommendViewModel
import com.ghn.routermodule.AppRouter


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
            AppRouter.openHome()
        }
        mBinding.TvNavElse.setOnClickListener {
           AppRouter.openUserKey()
        }
        mBinding.TvNavKey.setOnClickListener {
            AppRouter.openWeb("https://www.baidu.com/")
        }

    }

    override fun initData() {
    }

    override fun initViewObservable() {
    }
}
