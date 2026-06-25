package com.ghn.cocknovel.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * @author 浩楠
 *
 * @date 2023/1/10   13:27.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述:
 */
class TabLayoutAdapter(
    fragment: Fragment,
    private val fragmentList: List<Fragment>,
    private val listTitle: List<String>
) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment = fragmentList[position]

    override fun getItemCount(): Int = listTitle.size

    fun getPageTitle(position: Int): CharSequence = listTitle[position]
}
