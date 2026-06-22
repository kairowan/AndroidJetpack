package com.ghn.cocknovel.viewmodel

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.cocknovel.model.BannerItem
import com.ghn.cocknovel.repository.RecommendRepository
import com.ghn.cocknovel.ui.activity.WebviewActivity
import com.kt.network.bean.ProjectBean
import com.kt.network.bean.TabFrameBean
import com.kt.network.bean.FontDataNew
import org.koin.android.annotation.KoinViewModel

/**
 * @author 浩楠
 *
 * @date 2023/4/6-17:16.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
@KoinViewModel
open class RecommendViewModel(
    application: Application,
    private val recommendRepository: RecommendRepository
) : BaseViewModel(application) {
    companion object {
        val TAG: String? = RecommendViewModel::class.simpleName
    }

    val homeStatus = MutableLiveData<FontDataNew>()
    val mBanner = MutableLiveData<MutableList<BannerItem>>()
    val mProject = MutableLiveData<MutableList<ProjectBean.Data>>()
    val mProjectcontent = MutableLiveData<TabFrameBean.Data>()

    open fun getBanner() {
        launchGo({
            recommendRepository.getBanner().also {
                mBanner.value = it.data
                    ?.mapTo(mutableListOf()) { banner -> BannerItem(imagePath = banner.imagePath) }
                    ?: mutableListOf()
                Log.d(TAG, "getBanner: errorCode=${it.errorCode}")
            }
        })
    }

    open fun getHomeStatus(page: Int) {
        launchOnlyresult({
            recommendRepository.getHomeStatus(page)
        }, {
            homeStatus.value = it
        }, {}, {}, false)
    }

    open fun setWebview(url: String) {
        val bundle = Bundle()
        bundle.putString("url", url)
        startActivity(WebviewActivity::class.java, bundle, 1000)
    }

    open fun getProject() {
        launchOnlyresult({
            recommendRepository.getProject()
        }, {
//            mProject.value = it
        })
    }

    open fun project_content(page: Int, cid: Int) {
        launchOnlyresult({
            recommendRepository.getProjectContent(page, cid)
        }, {
            Log.i(TAG, "project_content: ${it}")
//            mProjectcontent.value = it
        })
    }

    open fun dow() {
    }
}
