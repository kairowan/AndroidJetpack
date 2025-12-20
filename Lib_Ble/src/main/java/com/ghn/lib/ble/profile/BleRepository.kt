package com.ghn.lib.ble.profile

import android.content.Context
import com.ghn.lib.ble.callback.BleDataObserver
import com.ghn.lib.ble.core.CBleManager

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
object BleRepository {

    private lateinit var bleManager: CBleManager

    /**
     * 全局初始化
     */
    fun init(context: Context) {
        bleManager = CBleManager.getInstance(context)

    }

    /**
     * 获取 Manager
     */
    fun getManager(): CBleManager = bleManager

    /**
     * 连接
     */
    fun connect(device: android.bluetooth.BluetoothDevice) {
        bleManager.connect(device)
            .retry(3, 100)
            .useAutoConnect(false)
            .enqueue()
    }

    /**
     * 断开
     */
    fun disconnect() {
        bleManager.disconnect().enqueue()
    }
}