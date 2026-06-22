package com.kt.network.net.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.annotation.SuppressLint
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

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
                if (context == null) return -1
                val conMan = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = conMan.activeNetwork ?: return -1
                    val capabilities = conMan.getNetworkCapabilities(network) ?: return -1
                    return when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 1
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 3
                        else -> 1
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val info = conMan.activeNetworkInfo
                    @Suppress("DEPRECATION")
                    if (info != null && info.isConnected) {
                        return if (info.type == ConnectivityManager.TYPE_WIFI) 1 else 3
                    }
                }
            } catch (_: Exception) {
                return 1
            }
            return -1
        }
    }
}