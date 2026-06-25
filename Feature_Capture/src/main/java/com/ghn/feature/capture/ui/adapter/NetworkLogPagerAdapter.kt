package com.ghn.feature.capture.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Author: zpj
 * Date: 2023-09-05 15:41
 * Desc: 日志详情适配器
 */
class NetworkLogPagerAdapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    internal fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getItemCount() = mFragmentList.size

    override fun createFragment(position: Int) = mFragmentList[position]

    fun getPageTitle(position: Int) = mFragmentTitleList[position]

}
