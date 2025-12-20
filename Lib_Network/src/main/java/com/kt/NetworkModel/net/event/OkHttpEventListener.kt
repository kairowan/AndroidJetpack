package com.kt.ktmvvm.net.event

import android.util.Log
import com.kt.network.net.event.OkHttpEvent
import okhttp3.Call
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.EventListener.Factory
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

class OkHttpEventListener : EventListener() {


    private var OkHttpEvent: OkHttpEvent? = null

    companion object {
        val TAG = OkHttpEventListener::class.simpleName
        val FACTORY = Factory {
            OkHttpEventListener()
        }
    }

    init {
        OkHttpEvent = OkHttpEvent()
    }

    override fun callStart(call: Call) {
        super.callStart(call)
        OkHttpEvent?.callStartTime = System.currentTimeMillis()
    }

    /**
     * dns解析开始
     */
    override fun dnsStart(call: Call, domainName: String) {
        super.dnsStart(call, domainName)
        OkHttpEvent?.dnsStartTime = System.currentTimeMillis()
    }

    /**
     * dns 解析结束
     */
    override fun dnsEnd(
        call: Call,
        domainName: String,
        inetAddressList: List<@JvmSuppressWildcards InetAddress>
    ) {
        super.dnsEnd(call, domainName, inetAddressList)
        OkHttpEvent?.dnsEndTime = System.currentTimeMillis()
    }

    /**
     * 连接开始
     */
    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        super.connectStart(call, inetSocketAddress, proxy)
        OkHttpEvent?.connectStartTime = System.currentTimeMillis()
    }

    /**
     *连接结束
     */
    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?
    ) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol)
        OkHttpEvent?.connectEndTime = System.currentTimeMillis()
    }

    /**
     * 安全连接开始
     */
    override fun secureConnectStart(call: Call) {
        super.secureConnectStart(call)
        OkHttpEvent?.secureConnectStart = System.currentTimeMillis()
    }

    /**
     * 安全连接结束
     */
    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        super.secureConnectEnd(call, handshake)
        OkHttpEvent?.secureConnectEnd = System.currentTimeMillis()
    }


    override fun connectFailed(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?,
        ioe: IOException
    ) {
        super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe)
    }

    override fun connectionAcquired(call: Call, connection: Connection) {
        super.connectionAcquired(call, connection)
    }

    override fun connectionReleased(call: Call, connection: Connection) {
        super.connectionReleased(call, connection)
    }

    override fun requestHeadersStart(call: Call) {
        super.requestHeadersStart(call)
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        super.requestHeadersEnd(call, request)
    }

    override fun requestBodyStart(call: Call) {
        super.requestBodyStart(call)
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        super.requestBodyEnd(call, byteCount)
    }

    override fun responseHeadersStart(call: Call) {
        super.responseHeadersStart(call)
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        super.responseHeadersEnd(call, response)
    }

    override fun responseBodyStart(call: Call) {
        super.responseBodyStart(call)
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        super.responseBodyEnd(call, byteCount)
        OkHttpEvent?.responseBodySize = byteCount
    }

    override fun callEnd(call: Call) {
        super.callEnd(call)
        OkHttpEvent?.callEndTime = System.currentTimeMillis()
        // 记录 API 请求成功
        OkHttpEvent?.apiSuccess = true
        OkHttpEvent?.toString()?.let { Log.i(TAG, it) }
    }

    override fun callFailed(call: Call, ioe: IOException) {
        super.callFailed(call, ioe)
        // 记录 API 请求失败及原因
        OkHttpEvent?.apiSuccess = false
        OkHttpEvent?.errorReason = Log.getStackTraceString(ioe)
        Log.i(TAG, "reason " + OkHttpEvent?.errorReason)
        OkHttpEvent?.toString()?.let { Log.i(TAG, it) }
    }
}