package com.kt.NetworkModel.net.interceptor

import android.util.Log
import com.kt.NetworkModel.helper.LogJsonUtils
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.Platform.Companion.INFO
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * @author 浩楠
 *
 * @date 2023/5/12-13:35.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
class LoggingInterceptor : Interceptor {

    private var tag: String = "HttpLogging"
    var isDebug: Boolean = false
    var type = INFO
    var requestTag: String = tag
    var responseTag: String = tag
    var level = Level.BASIC
    private val headers = Headers.Builder()
    var logger: Logger? = null


    interface Logger {
        fun log(level: Int, tag: String, msg: String)

        companion object {
            //            val DEFAULT: Logger = object : Logger {
//                override fun log(level: Int, tag: String, msg: String) {
//                    Platform.get().log(msg,level,  null)
//                }
//            }
            val DEFAULT: Logger = object : Logger {
                override fun log(level: Int, tag: String, msg: String) {
                    Log.println(level, tag, msg)
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestId = Printer.generateId()
        var request = chain.request()
        if (getHeaders().size > 0) {
            val headers = request.headers
            val names = headers.names()
            val iterator = names.iterator()
            val requestBuilder = request.newBuilder()
            requestBuilder.headers(getHeaders())
            while (iterator.hasNext()) {
                val name = iterator.next()
                requestBuilder.addHeader(name, headers.get(name)!!)
            }
            request = requestBuilder.build()
        }

        if (!isDebug || level == Level.NONE) {
            return chain.proceed(request)
        }
        val requestBody = request.body

        var rContentType: MediaType? = null
        if (requestBody != null) {
            rContentType = request.body!!.contentType()
        }

        var rSubtype: String? = null
        if (rContentType != null) {
            rSubtype = rContentType.subtype
        }

        if (rSubtype != null && (rSubtype.contains("json")
                    || rSubtype.contains("xml")
                    || rSubtype.contains("plain")
                    || rSubtype.contains("html"))
        ) {
            Printer.printJsonRequest(this, request, requestId)
        } else {
            Printer.printFileRequest(this, request, requestId)
        }

        val st = System.nanoTime()
        val response = chain.proceed(request)

        val segmentList = request.url.encodedPathSegments
        val chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st)
        val header = response.headers.toString()
        val code = response.code
        val isSuccessful = response.isSuccessful
        val responseBody = response.body
        val contentType = responseBody!!.contentType()

        var subtype: String? = null
        val body: ResponseBody

        if (contentType != null) {
            subtype = contentType.subtype
        }

        if (subtype != null && (subtype.contains("json")
                    || subtype.contains("xml")
                    || subtype.contains("plain")
                    || subtype.contains("html"))
        ) {
            val bodyString = responseBody.string()
//            val bodyJson = JsonUtils.formatJson(bodyString)
            val bodyJson = LogJsonUtils.formatJson(bodyString)
            Printer.printJsonResponse(
                this,
                chainMs,
                isSuccessful,
                code,
                header,
                bodyJson,
                segmentList,
                requestId
            )
            body = ResponseBody.create(contentType, bodyString)
//            val charset = body.contentType()?.charset(Charset.forName("UTF-8"))
//                ?: Charset.forName("UTF-8")
        } else {
            Printer.printFileResponse(
                this,
                chainMs,
                isSuccessful,
                code,
                header,
                segmentList,
                requestId
            )
            return response
        }
        return response.newBuilder().body(body).build()
    }

    private fun getHeaders(): Headers = headers.build()
}