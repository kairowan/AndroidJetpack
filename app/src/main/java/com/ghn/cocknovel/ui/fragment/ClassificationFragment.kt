package com.ghn.cocknovel.ui.fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.basemodel.base.basefra.BaseFragment
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.FragmentClassificationBinding
import com.ghn.cocknovel.viewmodel.RecommendViewModel
import com.google.android.material.tabs.TabLayout


class ClassificationFragment : BaseFragment<FragmentClassificationBinding, RecommendViewModel>() {

    private lateinit var fragmentList: MutableList<Fragment>
//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentClassificationBinding = FragmentClassificationBinding.inflate(inflater,container,false)


    override fun initParam() {

    }

    override fun initView() {
        //初始化fragmentlist
        fragmentList=ArrayList()
        //viewpage2禁止预加载
        mBinding.ficationPage.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
    }

    override fun initData() {
        mViewModel.getProject()

    }

    override fun initViewObservable() {


        mViewModel.mProject.observe(viewLifecycleOwner) {
            for (i in 0 until it.size) {
                mBinding.ficationTab.addTab(mBinding.ficationTab.newTab().setText(it[i].name))
                fragmentList.add(TabFragment())
            }
            activity?.supportFragmentManager.let {
                mBinding.ficationPage.setAdapter(object : FragmentStateAdapter(it!!, lifecycle) {
                    override fun createFragment(position: Int)=fragmentList[position]
                    override fun getItemCount(): Int= fragmentList.size

                })

            }
            mBinding.ficationTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    for (i in 0 until it.size){
                        TabFragment.tabling(it[tab!!.position].id)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    for (i in 0 until it.size){
                        TabFragment.tabling(it[tab!!.position].id)
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    for (i in 0 until it.size){
                        TabFragment.tabling(it[tab!!.position].id)
                    }
                }

            })
            mBinding.ficationPage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    TabFragment.tabling(it[position].id)
                    mBinding.ficationTab.selectTab( mBinding.ficationTab.getTabAt(position))
                }
            })
        }

    }
}
