package com.kt.network.net

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*


class TrustAllCerts : X509TrustManager {
    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {

    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, p1: String?) {
        requireNotNull(chain) { "  Check Server x509Certificates is null" }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate?> {
        return arrayOfNulls(0)
    }


    companion object {
        fun createSSLSocketFactory(): SSLSocketFactory? {
            var ssfFactory: SSLSocketFactory? = null
            try {
                val sc = SSLContext.getInstance("TLS")
                sc.init(
                    null, arrayOf(TrustAllCerts()), SecureRandom()
                )
                ssfFactory = sc.socketFactory
            } catch (e: Exception) {
            }
            return ssfFactory
        }
    }


    class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }
}