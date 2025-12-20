package com.ghn.feature.capture.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Author: zpj
 * Date: 2023-09-07 16:02
 * Desc: SP工具类
 */
internal val Context.sp: SharedPreferences get() = getSharedPreferences("cp_config", Context.MODE_PRIVATE)

// 是否打开抓包开关
internal var Context.isOpenNetworkCapture: Boolean
    get() = sp.getBoolean("isOpenNetworkCapture", true)
    set(value) = sp.edit().putBoolean("isOpenNetworkCapture", value).apply()

// 是否折叠请求头
internal var Context.isFoldRequestHeaders: Boolean
    get() = sp.getBoolean("isFoldRequestHeaders", true)
    set(value) = sp.edit().putBoolean("isFoldRequestHeaders", value).apply()

// 是否折叠响应头
internal var Context.isFoldResponseHeaders: Boolean
    get() = sp.getBoolean("isFoldResponseHeaders", true)
    set(value) = sp.edit().putBoolean("isFoldResponseHeaders", value).apply()

// 是否在Logcat打印请求日志信息
internal var Context.isPrintNetworkLog: Boolean
    get() = sp.getBoolean("isPrintNetworkLog", false)
    set(value) = sp.edit().putBoolean("isPrintNetworkLog", value).apply()