package com.ghn.cocknovel.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.cocknovel.model.BannerItem
import com.ghn.cocknovel.repository.RecommendRepository
import com.ghn.lib.base.aop.network.RequireNetwork
import com.ghn.routermodule.AppRouter
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
 * @Description: 推荐页 ViewModel，负责组织首页推荐相关状态与页面交互。
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

    @RequireNetwork(message = "当前无网络，无法加载 Banner")
    open fun getBanner() {
        launchGo({
            recommendRepository.getBanner().also {
                // 统一转换成 UI 层消费的数据结构，避免页面层重复做映射。
                mBanner.value = it.data
                    ?.mapTo(mutableListOf()) { banner -> BannerItem(imagePath = banner.imagePath) }
                    ?: mutableListOf()
                Log.d(TAG, "getBanner: errorCode=${it.errorCode}")
            }
        })
    }

    @RequireNetwork(message = "当前无网络，无法加载首页内容")
    open fun getHomeStatus(page: Int) {
        launchOnlyresult({
            recommendRepository.getHomeStatus(page)
        }, {
            homeStatus.value = it
        }, {}, {}, false)
    }

    open fun setWebview(url: String) {
        AppRouter.openPlainWeb(url)
    }

    @RequireNetwork(message = "当前无网络，无法加载项目分类")
    open fun getProject() {
        launchOnlyresult({
            recommendRepository.getProject()
        }, {
//            mProject.value = it
        })
    }

    @RequireNetwork(message = "当前无网络，无法加载项目列表")
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
