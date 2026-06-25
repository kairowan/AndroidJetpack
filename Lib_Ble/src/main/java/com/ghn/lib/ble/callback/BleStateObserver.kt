package com.ghn.lib.ble.callback

import android.bluetooth.BluetoothDevice

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
 * @Description: TODO 放对外暴露
 */
interface BleStateObserver {

    /**
     * 开始尝试连接
     */
    fun onConnecting(device: BluetoothDevice) {}

    /**
     * 物理层连接成功
     */
    fun onConnected(device: BluetoothDevice) {}

    /**
     * 设备准备就绪
     */
    fun onDeviceReady(device: BluetoothDevice) {}

    /**
     * 设备正在断开
     */
    fun onDisconnecting(device: BluetoothDevice) {}

    /**
     * 设备已断开
     */
    fun onDisconnected(device: BluetoothDevice, reason: Int) {}

    /**
     * 连接失败
     */
    fun onConnectFailed(device: BluetoothDevice, reason: Int) {}
}