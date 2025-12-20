package com.ghn.feature.capture.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * Author: zpj
 * Date: 2023-09-05 15:41
 * Desc: 日志详情适配器
 */
class NetworkLogPagerAdapter constructor(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    internal fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getCount() = mFragmentList.size

    override fun getItem(position: Int) = mFragmentList[position]

    override fun getPageTitle(position: Int) = mFragmentTitleList[position]

}