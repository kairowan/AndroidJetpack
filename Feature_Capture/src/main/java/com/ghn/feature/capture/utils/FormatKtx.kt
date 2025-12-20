package com.ghn.feature.capture.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Author: zpj
 * Date: 2023-09-04 11:35
 * Desc: 字符串格式化扩展
 */

/* *************************************************************
 *                           日期时间格式化
 ************************************************************ */

val TIME_SHORT = SimpleDateFormat("HH:mm:ss", Locale.CHINA)

val TIME_LONG = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

fun Date?.formatData(format: SimpleDateFormat = TIME_SHORT): String =
    if (this == null) "" else format.format(this)

/* *************************************************************
 *                           字符串格式化
 ************************************************************ */

/**
 * 字符串为空或空字符串设置默认值，否则返回字符串本身
 * */
fun String?.noDataOrThis(fillString: String = "无") = if (this.isNullOrBlank()) fillString else this

fun Long?.noDataOrThis(fillString: String = "无") = "$this" ?: fillString

fun String?.nullOrThis() = if (this.isNullOrBlank()) null else this

/**
 * 字节数格式化
 * */
internal fun bytesToStr(bytes: Long?): String {
    if (bytes == null) return ""
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    return "%.2f %s".format(size, units[unitIndex])
}