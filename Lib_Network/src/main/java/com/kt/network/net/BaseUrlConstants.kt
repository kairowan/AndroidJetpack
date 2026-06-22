package com.kt.network.net

class BaseUrlConstants {
    companion object {
        fun getHost(host: Int): String {
            return NetworkHost.fromType(host).baseUrl
        }

        fun getHost(host: NetworkHost): String {
            return host.baseUrl
        }
    }
}
