package com.ghn.lib.ble.utils

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
 * @Description: TODO hex转换 蓝牙权限检查
 */
object BleUtils {

    /**
     * ByteArray 转 Hex String
     */
    fun toHexString(bytes: ByteArray?): String {
        if (bytes == null || bytes.isEmpty()) return ""
        val sb = StringBuilder()
        for (b in bytes) {
            val hex = Integer.toHexString(b.toInt() and 0xFF)
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex).append(" ")
        }
        return sb.toString().trim().uppercase()
    }

    /**
     * Hex String 转 ByteArray
     */
    fun hexToBytes(hexString: String): ByteArray {
        val cleanInput = hexString.replace(" ", "")
        val len = cleanInput.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(cleanInput[i], 16) shl 4) +
                    Character.digit(cleanInput[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}