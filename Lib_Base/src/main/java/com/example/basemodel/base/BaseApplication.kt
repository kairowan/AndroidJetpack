package com.example.basemodel.base

import android.app.Application
import android.util.Log
import com.example.basemodel.base.init.AppHeaderProvider
import com.example.basemodel.base.init.NetworkCallbackImpl
import com.ghn.eventmodule.EventChannel
import com.ghn.commonmodule.ext.MVUtils
import com.kt.network.helper.NetConfigHelper
import com.kt.network.net.ExceptionHandle
import com.kt.network.net.NetServiceFactory
import com.kt.network.net.RetrofitClient
import com.tencent.mmkv.MMKV

/**
 * @author 浩楠
 *
 * @date 2025/12/20
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
open class BaseApplication : Application() {
    @Volatile
    private var baseRuntimeInitialized = false

    override fun onCreate() {
        super.onCreate()
    }

    @Synchronized
    fun ensureBaseRuntimeInitialized() {
        if (baseRuntimeInitialized) {
            return
        }
        this.initMMkv()
        // 初始化 handler头
        RetrofitClient.init(AppHeaderProvider())
        NetServiceFactory.init(this)
        // 初始化 Toast
        NetConfigHelper.init(NetworkCallbackImpl())
        EventChannel.setErrorHandler { t ->
            val ex = ExceptionHandle.handleException(t)
            Log.e("EventChannel", "Event error: ${ex.code} ${ex.errMsg}", t)
        }
        baseRuntimeInitialized = true
    }

    private fun initMMkv() {
        MMKV.initialize(this)
        MVUtils.instance
    }
}
