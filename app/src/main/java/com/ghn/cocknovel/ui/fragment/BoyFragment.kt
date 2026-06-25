package com.ghn.cocknovel.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.basemodel.base.basefra.BaseFragment
import com.ghn.cocknovel.databinding.FragmentBoyBinding
import com.ghn.cocknovel.viewmodel.RecommendViewModel
import com.ghn.routermodule.AppRouter


class BoyFragment : BaseFragment<FragmentBoyBinding, RecommendViewModel>(){
    override fun initContentView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBoyBinding = FragmentBoyBinding.inflate(inflater,container,false)


    override fun initParam() {

    }

    override fun initView() {
        mBinding.TvNav.setOnClickListener {
            AppRouter.openHome(requireActivity())
        }
        mBinding.TvNavElse.setOnClickListener {
           AppRouter.openUserKey(requireActivity())
        }
        mBinding.TvNavKey.setOnClickListener {
            AppRouter.openPlainWeb("https://www.baidu.com/", requireActivity())
        }

    }

    override fun initData() {
    }

    override fun initViewObservable() {
    }
}
