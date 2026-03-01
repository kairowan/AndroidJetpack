package com.kt.network.net.event

import android.text.TextUtils

/**
 * @author 浩楠
 *
 * @date 2026-2-17
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

class OkHttpEvent {
    var callStartTime: Long = 0
    var callEndTime: Long = 0
    var dnsStartTime: Long = 0
    var dnsEndTime: Long = 0
    var connectStartTime: Long = 0
    var connectEndTime: Long = 0
    var secureConnectStart: Long = 0
    var secureConnectEnd: Long = 0
    var responseBodySize: Long = 0
    var apiSuccess = false
    var errorReason: String? = null


    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("NetData: [").append("\n")
        sb.append("callTime: ").append(callEndTime - callStartTime).append("\n")
        sb.append("dnsParseTime: ").append(dnsEndTime - dnsStartTime).append("\n")
        sb.append("connectTime: ").append(connectEndTime - callStartTime).append("\n")
        sb.append("secureConnectTime: ").append(secureConnectEnd - secureConnectStart).append("\n")
        sb.append("responseBodySize: ").append(responseBodySize).append("\n")
        sb.append("apiSuccess: ").append(apiSuccess).append("\n")
        if (!TextUtils.isEmpty(errorReason)) {
            sb.append("errorReason: ").append(errorReason).append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

}