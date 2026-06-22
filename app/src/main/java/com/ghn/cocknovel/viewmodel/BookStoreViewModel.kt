package com.ghn.cocknovel.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ghn.cocknovel.ui.activity.MainActivity
import com.ghn.cocknovel.ui.activity.SetActivity
import com.ghn.cocknovel.ui.activity.SwitchActivity
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.eventmodule.EventChannel
import com.ghn.eventmodule.collectIn
import com.ghn.module_login.repository.LoginRepository
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
 * 描述:
 */

sealed class GlobalEvent {
    data class TokenExpired(val reason: String) : GlobalEvent()
    object ForceLogout : GlobalEvent()
    data class AppLanguageChanged(val language: String) : GlobalEvent()
}

@KoinViewModel
open class BookStoreViewModel(
    application: Application,
    private val loginRepository: LoginRepository
) : BaseViewModel(application) {

    companion object {
        val TAG: String? = BookStoreViewModel::class.simpleName
    }

    init {
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
        startActivity(SetActivity::class.java)
    }

    /**
     * 全局更换字体
     */
    open fun getSwitchFont() {
        //App.get().changeTTF()
        startActivity(SwitchActivity::class.java)
    }
}
