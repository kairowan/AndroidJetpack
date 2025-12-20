package com.kt.network.net.interceptor

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
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
                if (context == null) {
                    return -1
                }
                val conMan = context.getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager
                val info = conMan.activeNetworkInfo
                if (null != info && info.isConnected) {
                    if (info.type == ConnectivityManager.TYPE_MOBILE) {
                        return when (info.subtype) {
                            1, 2, 4 ->                             // 2G网络
                                2
                            else ->                             // 3G及其以上网络
                                3
                        }
                    } else if (info.type == ConnectivityManager.TYPE_WIFI) {
                        // wifi网络
                        return 1
                    }
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