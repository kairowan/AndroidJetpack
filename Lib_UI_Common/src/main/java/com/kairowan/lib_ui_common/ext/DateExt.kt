package com.kairowan.lib_ui_common.ext

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * @author 浩楠
 * @date 2025/6/11 15:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 日期时间相关扩展函数
 */

// Long → 时间字符串（默认格式：yyyy-MM-dd HH:mm:ss）
fun Long.toDateStr(pattern: String = "yyyy-MM-dd HH:mm:ss"): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))

// String → 时间戳（默认格式：yyyy-MM-dd HH:mm:ss）
fun String.toTimestamp(pattern: String = "yyyy-MM-dd HH:mm:ss"): Long =
    try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }

// 是否为今天
fun Long.isToday(): Boolean {
    val now = Calendar.getInstance()
    val that = Calendar.getInstance().apply { timeInMillis = this@isToday }
    return now.get(Calendar.YEAR) == that.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == that.get(Calendar.DAY_OF_YEAR)
}

// 是否为昨天
fun Long.isYesterday(): Boolean {
    val now = Calendar.getInstance()
    val that = Calendar.getInstance().apply { timeInMillis = this@isYesterday }

    now.add(Calendar.DAY_OF_YEAR, -1)
    return now.get(Calendar.YEAR) == that.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == that.get(Calendar.DAY_OF_YEAR)
}

// 转换为“几分钟前 / 几小时前 / 几天前 / 日期”
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    val minute = 60 * 1000
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diff < minute -> "刚刚"
        diff < hour -> "${diff / minute} 分钟前"
        diff < day -> "${diff / hour} 小时前"
        diff < 3 * day -> "${diff / day} 天前"
        else -> this.toDateStr("yyyy-MM-dd")
    }
}

// 获取当前时间戳
fun currentTimeMillis(): Long = System.currentTimeMillis()

// 获取当前时间字符串（默认 yyyy-MM-dd HH:mm:ss）
fun now(pattern: String = "yyyy-MM-dd HH:mm:ss"): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date())

// 判断是否早于指定小时数
fun Long.isBeforeHours(hours: Int): Boolean {
    val now = System.currentTimeMillis()
    return now - this > hours * 3600_000
}
