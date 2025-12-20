package com.ghn.cocknovel.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.basemodel.base.basefra.BaseFragment
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.FragmentBookstoreBinding
import com.ghn.cocknovel.ui.adapter.TabLayoutAdapter
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener


class BookstoreFragment : BaseFragment<FragmentBookstoreBinding, BookStoreViewModel>() {
    private val fragmentList = ArrayList<Fragment>()
    var titles = ArrayList<String>()
//    override fun initVariableId(): Int {
//        return BR.model
//    }

    override fun initContentView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBookstoreBinding = FragmentBookstoreBinding.inflate(inflater,container,false)


    override fun initParam() {

    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun initViewObservable() {
        fragmentList.clear()
        titles.clear()
        fragmentList.add(RecommendFragment())
        fragmentList.add(BoyFragment())
        fragmentList.add(GirlFragment())
        titles.add("数据页面")
        titles.add("路由页面")
        titles.add("空")
        mBinding.viewpage.adapter=
            activity.let { TabLayoutAdapter(it!!.supportFragmentManager,fragmentList,titles) }
        mBinding.mainTab.setupWithViewPager( mBinding.viewpage)
        mBinding.mainTab.getTabAt(0)!!.view.scaleX=1.5f
        mBinding.mainTab.getTabAt(0)!!.view.scaleY=1.5f
        mBinding.mainTab.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                (tab.view as View).setScaleX(1.5f)
                (tab.view as View).setScaleY(1.5f)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                (tab.view as View).scaleX = 0.8f
                (tab.view as View).scaleY = 0.8f
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
                (tab.view as View).scaleX = 1.5f
                (tab.view as View).scaleY = 1.5f
            }
        })
    }
}