package com.ghn.cocknovel.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ghn.cocknovel.ui.activity.MainActivity
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.eventmodule.EventChannel
import com.ghn.eventmodule.collectIn
import com.ghn.lib.base.aop.network.RequireNetwork
import com.ghn.module_login.repository.LoginRepository
import com.ghn.routermodule.aop.guard.PreventRepeat
import com.ghn.routermodule.AppRouter
import org.koin.android.annotation.KoinViewModel

/**
 * @author 浩楠
 *
 * @date 2023/1/9   16:11.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用内全局事件定义，供登录流程和页面状态协同使用。
 */

sealed class GlobalEvent {
    data class TokenExpired(val reason: String) : GlobalEvent()
    object ForceLogout : GlobalEvent()
    data class AppLanguageChanged(val language: String) : GlobalEvent()
}

/**
 * 书城页/登录入口 ViewModel，负责处理验证码登录请求和常用页面导航。
 */
@KoinViewModel
open class BookStoreViewModel(
    application: Application,
    private val loginRepository: LoginRepository
) : BaseViewModel(application) {

    companion object {
        val TAG: String? = BookStoreViewModel::class.simpleName
    }

    init {
        // sticky=true 可以在 ViewModel 重建后补收到最近一次全局状态变更。
        EventChannel.observe<GlobalEvent>(sticky = true)
            .collectIn(viewModelScope) {
                handleGlobalEvent(it)
            }
    }

    private fun handleGlobalEvent(event: GlobalEvent) {
        when (event) {
            is GlobalEvent.TokenExpired -> { /* 处理过期 */ }
            is GlobalEvent.ForceLogout -> { /* 强制退出 */ }
            is GlobalEvent.AppLanguageChanged -> { /* 语言切换 */ }
        }
    }

    /**
     * 跳转到首页
     */
    @RequireNetwork(message = "当前无网络，暂时无法登录")
    @PreventRepeat(
        intervalMillis = 1500L,
        key = "login_request_verify_code",
        message = "请求过于频繁，请稍后再试"
    )
    open fun getMain(phoneNumber: String) {
        launchOnlyresult({
            loginRepository.requestVerifyCode(phoneNumber)
        }, {
            Log.i(TAG, "getMain: $it")
            startActivity(MainActivity::class.java)
        })
    }

    /**
     * 从我的页面跳转到设置页面
     */
    open fun getsetImage() {
        AppRouter.openSettings()
    }

    /**
     * 全局更换字体
     */
    open fun getSwitchFont() {
        AppRouter.openFontSettings()
    }
}
