package com.ghn.feature.capture.utils

import android.util.Log
import com.ghn.feature.capture.entity.NetworkLog

/**
 * Author: zpj
 * Date: 2023-09-05 15:09
 * Desc: 日志工具类
 */


object LogHelper {
    const val TAG = "CpNetworkCapture"
    private const val TOP_LEFT_CORNER = '┌'
    private const val BOTTOM_LEFT_CORNER = '└'
    private const val MIDDLE_CORNER = '├'
    private const val HORIZONTAL_LINE = '│'
    private const val DOUBLE_DIVIDER = "────────────────────────────────────────────────────────"
    private const val SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
    private const val TOP_BORDER = TOP_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
    private const val BOTTOM_BORDER = BOTTOM_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
    private const val MIDDLE_BORDER = MIDDLE_CORNER.toString() + SINGLE_DIVIDER + SINGLE_DIVIDER

    /**
     * 格式化输出请求日志的方法
     * */
    fun printNetworkLog(log: NetworkLog) {
        log.apply {
            printTop()
            printContent("Request URL：${log.url} $HORIZONTAL_LINE ${log.method} $HORIZONTAL_LINE ${log.protocol}")
            printMiddle()
            if (requestHeaders.isNullOrBlank()) {
                printContent("Request Headers：NULL")
            } else {
                printContent("Request Headers：")
                GsonHelper.fromJsonArray(
                    requestHeaders!!,
                    HttpHeader::class.java
                ).forEach { printContent("${it.name}:${it.value}") }
            }
            printMiddle()
            if (requestBody.isNullOrBlank()) {
                printContent("Request Body：NULL")
            } else {
                printContent("Request Body：")
                printMiddle()
                requestBody!!.split("\n").forEach {
                    printContent(it)
                }
            }
            printBottom()
            printTop()
            printContent("Response Code：${log.responseCode} $HORIZONTAL_LINE Duration：${log.duration}ms")
            printMiddle()
            if (responseHeaders.isNullOrBlank()) {
                printContent("Response Headers：NULL")
            } else {
                printContent("Response Headers：")
                printMiddle()
                GsonHelper.fromJsonArray(
                    responseHeaders!!,
                    HttpHeader::class.java
                ).forEach { printContent("${it.name}:${it.value}") }
            }
            printMiddle()
            if (errorMsg.isNullOrBlank()) {
                if (responseBody.isNullOrBlank()) {
                    printContent("Response Body：NULL")
                } else {
                    printContent("Response Body：")
                    printMiddle()
                    responseBody!!.split("\n").forEach {
                        printContent(it)
                    }
                }
            } else {
                printContent("Response Body：${errorMsg}")
            }
            printBottom()
        }
    }

    private fun printTop(priority: Int = Log.DEBUG) = Log.println(priority, TAG, TOP_BORDER)

    private fun printMiddle(priority: Int = Log.DEBUG) = Log.println(priority, TAG, MIDDLE_BORDER)

    private fun printBottom(priority: Int = Log.DEBUG) = Log.println(priority, TAG, BOTTOM_BORDER)

    private fun printContent(msg: String? = null, priority: Int = Log.DEBUG) =
        Log.println(priority, TAG, "$HORIZONTAL_LINE $msg")

}