package com.example.basemodel.base.init

import android.os.Build
import com.blankj.utilcode.util.AppUtils
import com.ghn.commonmodule.ext.MVUtils
import com.kt.NetworkModel.provider.IHeaderProvider
import com.kt.ktmvvm.lib.BuildConfig
import java.util.Locale
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
 * @Description: TODO
 */
class AppHeaderProvider : IHeaderProvider {
    override fun getHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        val token = MVUtils.getString("token", "")
        if (!token.isNullOrEmpty()) {
             headers["Authorization"] = "Bearer $token"
        }

        headers["app-version"] = AppUtils.getAppVersionName()
        headers["app-code"] = AppUtils.getAppVersionCode().toString()

        headers["os-version"] = Build.VERSION.RELEASE
        headers["device-model"] = Build.MODEL
        headers["device-brand"] = Build.BRAND

        headers["language"] = Locale.getDefault().toLanguageTag()

        headers["X-Request-Id"] = UUID.randomUUID().toString()

        headers["timestamp"] = System.currentTimeMillis().toString()

        return headers
    }
}