package com.ghn.cocknovel.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.basemodel.base.basefra.BaseFragment
import com.example.basemodel.base.BaseRecyclerAdapter
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.FragmentTabBinding
import com.ghn.cocknovel.viewmodel.RecommendViewModel
import com.kt.NetworkModel.bean.TabFrameBean


class TabFragment : BaseFragment<FragmentTabBinding, RecommendViewModel>() {
    var adapter: BaseRecyclerAdapter<TabFrameBean.Data.Data>? = null

    companion object {
        var tabcid = 294
        val page = 0
        fun tabling(cid: Int) {
            tabcid = cid
        }
    }
    val recyclerview_tablist = mutableListOf<TabFrameBean.Data.Data>()

//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTabBinding =FragmentTabBinding.inflate(inflater,container,false)


    override fun initParam() {

    }

    override fun initView() {
        val controller = LayoutAnimationController(AnimationUtils.loadAnimation(activity, R.anim.animate))
        mBinding.fragmentRecyclerview.adapter = adapter
        mViewModel.mProjectcontent.observe(this) {
            adapter?.addData(it.datas)
        }
    }

    override fun initData() {
        tabcid.let { mViewModel.project_content(page, it) }

        mBinding.fragmentRecyclerview.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    override fun initViewObservable() {



        adapter = object : BaseRecyclerAdapter<TabFrameBean.Data.Data>(recyclerview_tablist) {
            @SuppressLint("CheckResult")
            override fun bindData(holder: BaseViewHolder?, position: Int) {
                holder?.itemView?.findViewById<TextView>(R.id.item_tab_title)?.text= datas[position].title
//                holder?.itemView?.item_tab_desc?.text = datas[position].desc
//                holder?.itemView?.item_tab_author?.text = datas[position].author
//                holder?.itemView?.item_tab_niceDate?.text = datas[position].niceDate
//                holder?.itemView?.item_tab_url?.let {
//                    Glide.with(this@TabFragment).load(datas[position].envelopePic).into(
//                        it
//                    )
//                }
//                holder?.itemView?.cst?.setOnClickListener {
//                    viewModel?.setWebview(datas[position].link)
//                }
            }

            override val layoutId: Int = R.layout.item_tab
        }


    }
}