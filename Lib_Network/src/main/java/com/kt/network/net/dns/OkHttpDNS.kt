package com.kt.network.net.dns

import android.content.Context
import android.util.Log
import com.alibaba.sdk.android.httpdns.HttpDns
import com.alibaba.sdk.android.httpdns.HttpDnsService
import okhttp3.Dns
import java.net.InetAddress

class OkHttpDNS(context: Context?) : Dns {
    private val SYSTEM = Dns.SYSTEM
    private var httpDns: HttpDnsService? = null

    init {
        httpDns = HttpDns.getService(context)
    }

    companion object {
        @Volatile
        private var instance: OkHttpDNS? = null

        fun get(context: Context?): OkHttpDNS {
            return instance ?: synchronized(OkHttpDNS::class.java) {
                instance ?: OkHttpDNS(context).also { instance = it }
            }
        }
    }

    override fun lookup(hostname: String): MutableList<InetAddress> {
        val ip = httpDns?.getIpByHostAsync(hostname)
        ip?.let {
            val inetAddresses = mutableListOf(*InetAddress.getAllByName(ip))
            Log.e("OkHttpDns", "inetAddresses:$inetAddresses")
            return inetAddresses
        } ?: let {
            return Dns.SYSTEM.lookup(hostname).toMutableList()
        }
    }
}
