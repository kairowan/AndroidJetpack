package com.ghn.feature.capture.entity

import com.ghn.feature.capture.utils.TIME_LONG
import java.io.Serializable
import java.util.*

/**
 * Author: zpj
 * Date: 2023-09-04 10:40
 * Desc: 网络日志实体类
 */
data class NetworkLog(
    var id: Long? = null,
    var method: String? = null,
    var url: String? = null,
    var scheme: String? = null,
    var protocol: String? = null,
    var host: String? = null,
    var path: String? = null,
    var duration: Long? = null,
    var requestTime: Long? = null,
    var requestHeaders: String? = null,
    var requestBody: String? = null,
    var requestContentType: String? = null,
    var responseCode: Int? = null,
    var responseTime: Long? = null,
    var responseHeaders: String? = null,
    var responseBody: String? = null,
    var responseMessage: String? = null,
    var responseContentType: String? = null,
    var responseContentLength: Long? = null,
    var errorMsg: String? = null,
    var source: String? = null
) : Serializable {
    fun getRequestTimeStr(): String {
        if (requestTime == null) return "无"
        else return TIME_LONG.format(Date(requestTime!!))
    }
    fun getResponseTimeStr(): String = if (requestTime == null) "无" else TIME_LONG.format(Date(responseTime!!))
}