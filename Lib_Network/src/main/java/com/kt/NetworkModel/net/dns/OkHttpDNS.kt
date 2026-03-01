package com.kt.network.net.dns


import android.content.Context
import android.util.Log
import com.alibaba.sdk.android.httpdns.HttpDns
import com.alibaba.sdk.android.httpdns.HttpDnsService
import okhttp3.Dns
import java.net.InetAddress

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

/**
 * DNS 优化
 */
class OkHttpDNS(context: Context?) : Dns {
    private val SYSTEM = Dns.SYSTEM
    private var httpDns: HttpDnsService? = null

    init {
        httpDns = HttpDns.getService(context)
    }


    companion object {
        private var instance: OkHttpDNS? = null
        fun get(context: Context?): OkHttpDNS {
            if (instance == null) {
                synchronized(OkHttpDNS::class.java) {
                    if (instance == null) {
                        instance = OkHttpDNS(context)
                    }
                }
            }
            return instance!!
        }
    }

    override fun lookup(hostname: String): MutableList<InetAddress> {
        //通过异步解析接⼝获取ip
        val ip = httpDns?.getIpByHostAsync(hostname)
        ip?.let {

            val inetAddresses = listOf(InetAddress.getAllByName(ip)) as MutableList<InetAddress>
            Log.e("OkHttpDns", "inetAddresses:$inetAddresses")
            return inetAddresses
        } ?: let {
            return Dns.SYSTEM.lookup(hostname).toMutableList()
        }
    }


}