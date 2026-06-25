package com.ghn.lib.ble.profile

import java.util.UUID

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
 * @Description: TODO 放 UUID 和 指令集
 */
object DeviceProfile {

    // 主服务 UUID
    val UUID_SERVICE: UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")

    // 写特征 UUID
    val UUID_CHAR_WRITE: UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

    // 通知特征 UUID
    val UUID_CHAR_NOTIFY: UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

    // 指令
    object Commands {

        val HANDSHAKE = byteArrayOf(0xAA.toByte(), 0x01.toByte())


        val GET_BATTERY = byteArrayOf(0xAA.toByte(), 0x02.toByte())


        fun createSyncTimeCmd(timestamp: Long): ByteArray {
            return byteArrayOf(
                0xAA.toByte(),
                0x03.toByte(),
                (timestamp shr 24).toByte(),
                (timestamp shr 16).toByte(),
                (timestamp shr 8).toByte(),
                timestamp.toByte()
            )
        }
    }
}