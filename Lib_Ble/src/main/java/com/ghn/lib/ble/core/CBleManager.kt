package com.ghn.lib.ble.core

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import com.ghn.lib.ble.callback.BleDataObserver
import com.ghn.lib.ble.callback.BleStateObserver
import com.ghn.lib.ble.profile.DeviceProfile
import com.ghn.lib.ble.utils.BleUtils
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.concurrent.CopyOnWriteArrayList

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
class CBleManager private constructor(context: Context) : BleManager(context) {

    private var writeChar: BluetoothGattCharacteristic? = null

    private var notifyChar: BluetoothGattCharacteristic? = null

    private val dataObservers = CopyOnWriteArrayList<BleDataObserver>()

    private val stateObservers = CopyOnWriteArrayList<BleStateObserver>()


    fun registerDataObserver(observer: BleDataObserver) {
        if (!dataObservers.contains(observer)) {
            dataObservers.add(observer)
        }
    }


    override fun getGattCallback(): BleManagerGattCallback {
        return MyGattCallback()
    }


    override fun log(priority: Int, message: String) {
        // 在 Logcat 中过滤 "CBleManager" 查看日志
        Log.println(priority, "CBleManager", message)

    }

    /**
     * 内部类 处理所有 GATT 事件
     */
    private inner class MyGattCallback : BleManagerGattCallback() {

        /**
         * 连接成功后会自动调用，用于检查设备是否符合我们的要求
         */
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service: BluetoothGattService? = gatt.getService(DeviceProfile.UUID_SERVICE)

            if (service != null) {
                writeChar = service.getCharacteristic(DeviceProfile.UUID_CHAR_WRITE)
                notifyChar = service.getCharacteristic(DeviceProfile.UUID_CHAR_NOTIFY)
            }

            val isSupported = (writeChar != null && notifyChar != null)
            if (!isSupported) {
                log(Log.WARN, "未发现必要的服务或特征，设备不匹配")
            }
            return isSupported
        }


        override fun initialize() {
            log(Log.INFO, "开始初始化设备...")

            requestMtu(512).enqueue()

            if (notifyChar != null) {
                setNotificationCallback(notifyChar)
                    .with { device, data ->
                        handleRecvData(device,data)
                    }
                enableNotifications(notifyChar)
                    .done { device -> log(Log.INFO, "通知已开启") }
                    .fail { device, status -> log(Log.ERROR, "通知开启失败: $status") }
                    .enqueue()
            }

            sendData(DeviceProfile.Commands.HANDSHAKE)
        }


        override fun onDeviceDisconnected() {
            writeChar = null
            notifyChar = null
            log(Log.WARN, "设备已断开连接")
        }

        override fun onServicesInvalidated() {

        }
    }

    /**
     * 处理接收到的数据
     */
    private fun handleRecvData(device: BluetoothDevice, data: Data) {
        val bytes = data.value ?: return
        val hexStr = BleUtils.toHexString(bytes)

        log(Log.INFO, "RX: $hexStr")
        dataObservers.forEach { it.onDataReceived(device, bytes, hexStr) }
    }


    /**
     * 发送数据
     */
    fun sendData(bytes: ByteArray) {
        if (writeChar == null) {
            log(Log.ERROR, "特征值未初始化，无法发送")
            return
        }

        log(Log.INFO, "TX: ${BleUtils.toHexString(bytes)}")
        writeCharacteristic(writeChar, bytes)
            .split()
            .done { device -> log(Log.DEBUG, "发送完成") }
            .fail { device, status -> log(Log.ERROR, "发送失败: $status") }
            .enqueue()
    }

    fun sendData(hexString: String) {
        sendData(BleUtils.hexToBytes(hexString))
    }


    /**
     * 注册状态监听
     */
    fun registerStateObserver(observer: BleStateObserver) {
        if (!stateObservers.contains(observer)) {
            stateObservers.add(observer)
        }
    }

    /**
     * 移除状态监听
     */
    fun unregisterStateObserver(observer: BleStateObserver) {
        stateObservers.remove(observer)
    }

    init {
        setConnectionObserver(object : ConnectionObserver {
            override fun onDeviceConnecting(device: BluetoothDevice) {
                stateObservers.forEach { it.onConnecting(device) }
            }

            override fun onDeviceConnected(device: BluetoothDevice) {
                stateObservers.forEach { it.onConnected(device) }
            }

            override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
                stateObservers.forEach { it.onConnectFailed(device, reason) }
            }

            override fun onDeviceReady(device: BluetoothDevice) {
                stateObservers.forEach { it.onDeviceReady(device) }
            }

            override fun onDeviceDisconnecting(device: BluetoothDevice) {
                stateObservers.forEach { it.onDisconnecting(device) }
            }

            override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
                stateObservers.forEach { it.onDisconnected(device, reason) }
            }
        })
    }

    companion object {
        @Volatile
        private var instance: CBleManager? = null

        fun getInstance(context: Context): CBleManager {
            return instance ?: synchronized(this) {
                instance ?: CBleManager(context.applicationContext).also { instance = it }
            }
        }
    }
}