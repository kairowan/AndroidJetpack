package com.ghn.lib.ble.base

import android.bluetooth.BluetoothDevice
import com.ghn.lib.ble.profile.BleRepository

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
 * @Description: TODO 业务基类
 */
abstract class BaseBleModule {

    /**
     * 处理数据
     * @return true 表示该数据已被消费/处理，不需要传给下一个模块了
     */
    abstract fun onReceive(device: BluetoothDevice, hex: String, data: ByteArray): Boolean

    /**
     * 统一的发送方法
     */
    protected fun send(bytes: ByteArray) {
        BleRepository.getManager().sendData(bytes)
    }
}