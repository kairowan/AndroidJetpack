package com.example.basemodel.base

import androidx.multidex.MultiDexApplication
import com.example.basemodel.base.init.AppHeaderProvider
import com.example.basemodel.base.init.NetworkCallbackImpl
import com.ghn.commonmodule.ext.MVUtils
import com.ghn.lib.ble.profile.BleRepository
import com.kt.NetworkModel.helper.NetConfigHelper
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
open class BaseApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        // Init MMKv
        this.initMMkv()
        // Init handler
        this.initHandler()
        // Init Toast
        this.initToast()
        // Init BleRepository
        this.initBle()
    }

    private fun initMMkv() {
        MMKV.initialize(this)
        MVUtils.instance
    }

    private fun initHandler() {
        RetrofitClient.init(AppHeaderProvider())
    }

    private fun initToast() {
        NetConfigHelper.init(NetworkCallbackImpl())

    }

    private fun initBle() {
        BleRepository.init(this)
    }
}