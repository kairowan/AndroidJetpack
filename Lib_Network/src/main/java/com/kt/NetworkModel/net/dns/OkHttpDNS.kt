package com.kt.network.net.dns


import android.content.Context
import android.util.Log
import com.alibaba.sdk.android.httpdns.HttpDns
import com.alibaba.sdk.android.httpdns.HttpDnsService
import okhttp3.Dns
import java.net.InetAddress


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