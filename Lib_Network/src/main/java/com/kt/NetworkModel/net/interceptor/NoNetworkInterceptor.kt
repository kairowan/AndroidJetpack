package com.kt.network.net.interceptor

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author 浩楠
 *
 * @date 2026-2-19
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

/**
 * 网络状态判断
 */
class NoNetworkInterceptor(private var context: Context?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newBuilder = request.newBuilder()

        if (getNetworkStatus(context) == -1) {
            //无网时，只从缓存中取
            newBuilder.cacheControl(CacheControl.FORCE_CACHE)
        } else {
            //有网时,只从服务器取
            newBuilder.cacheControl(CacheControl.FORCE_NETWORK)
        }
        return chain.proceed(newBuilder.build())
    }

    companion object {
        @SuppressLint("MissingPermission")
        fun getNetworkStatus(context: Context?): Int {
            try {
                if (context == null) {
                    return -1
                }
                val connectivityManager = context.getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager
                val network = connectivityManager.activeNetwork ?: return -1
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return -1
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return 1
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return 3
                }
            } catch (e: Exception) {
                //报错的就当有
                return 1
            }
            // 无网络
            return -1
        }
    }
}
