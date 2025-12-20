package com.ghn.feature.capture.interceptor

import NetworkCapture
import android.os.Build
import androidx.annotation.RequiresApi
import com.ghn.feature.capture.entity.NetworkLog
import com.ghn.feature.capture.utils.LogHelper
import com.ghn.feature.capture.utils.formatBody
import com.ghn.feature.capture.utils.isPrintNetworkLog
import com.ghn.feature.capture.utils.isProbablyUtf8
import com.ghn.feature.capture.utils.promisesBody
import com.ghn.feature.capture.utils.readString
import com.ghn.feature.capture.utils.toJsonString

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * Author: zpj
 * Date: 2023-09-05 15:11
 * Desc: 抓包拦截器
 */
class CaptureInterceptor : Interceptor {
    private var maxContentLength = 5L * 1024 * 1024

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val networkLog = NetworkLog().apply {
            method = request.method   // 请求方法
            request.url.toString().takeIf(String::isNotEmpty)?.let(URI::create)?.let { uri ->
                url = "$uri"    // 请求地址
                host = uri.host
                path = uri.path + if (uri.query != null) "?${uri.query}" else ""
                scheme = uri.scheme
                requestTime = System.currentTimeMillis()
            }
            requestHeaders = request.headers.toJsonString()   // 请求头
            request.body?.let { body -> body.contentType()?.let { requestContentType = "$it" } }
        }
        val startTime = System.nanoTime()   // 记录请求发起时间(微秒级别)
        val requestBody = request.body
        requestBody?.contentType()?.let { networkLog.requestContentType = "$it" }
        when {
            // 请求头为空、未知编码类、双工(可读可写)、请求体只能用一次
            requestBody == null || bodyHasUnknownEncoding(request.headers) || requestBody.isDuplex() || requestBody.isOneShot() -> {}
            // 上传文件
            requestBody is MultipartBody -> {
                networkLog.requestBody = StringBuilder().apply {
                    requestBody.parts.forEach {
                        val key = it.headers?.value(0)
                        append(
                            if (it.body.contentType()?.toString()?.contains("octet-stream") == true)
                                "${key}; value=文件流\n" else "${key}; value=${it.body.readString()}\n"
                        )
                    }
                }.toString()
            }
            else -> {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val charset = requestBody.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                if (buffer.isProbablyUtf8()) networkLog.requestBody =
                    formatBody(buffer.readString(charset), networkLog.requestContentType)
            }
        }

        val response: Response
        try {
            response = chain.proceed(request)
            networkLog.apply {
                responseHeaders = response.headers.toJsonString() // 响应头
                responseTime = System.currentTimeMillis()
                duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) // 当前时间减去请求发起时间得出响应时间
                protocol = response.protocol.toString()
                responseCode = response.code
                responseMessage = response.message
            }
            val responseBody = response.body
            responseBody?.contentType()?.let { networkLog.responseContentType = "$it" }
            val bodyHasUnknownEncoding = bodyHasUnknownEncoding(response.headers)
            // 响应体不为空、支持获取响应体、知道编码类型
            if (responseBody != null && response.promisesBody() && !bodyHasUnknownEncoding) {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE)  // 将响应体的内容都读取到缓冲区中
                var buffer = source.buffer  // 获取响应体源数据流
                // 如果响应体经过Gzip压缩，先解压缩
                if (bodyGzipped(response.headers)) {
                    GzipSource(buffer.clone()).use { gzippedResponseBody ->
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    }
                }
                // 获取不到字符集的话默认使用UTF-8 字符集
                val charset = responseBody.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                if (responseBody.contentLength() != 0L && buffer.isProbablyUtf8()) {
                    val body = readFromBuffer(buffer.clone(), charset)
                    networkLog.responseBody = formatBody(body, networkLog.responseContentType)
                }
                networkLog.responseContentLength = buffer.size
            }
            NetworkCapture.insertNetworkLog(networkLog)
            if (NetworkCapture.context!!.isPrintNetworkLog) LogHelper.printNetworkLog(networkLog)
            return response
        } catch (e: Exception) {
            networkLog.errorMsg = "$e"
            if (NetworkCapture.context!!.isPrintNetworkLog) LogHelper.printNetworkLog(networkLog)
            NetworkCapture.insertNetworkLog(networkLog)
            throw e
        }
    }

    // 检查头中的内容编码是否为除了 "identity" 和 "gzip" 外的其他未知编码类型
    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }

    // 判断响应头是否包含Gzip压缩
    private fun bodyGzipped(headers: Headers): Boolean {
        return "gzip".equals(headers["Content-Encoding"], ignoreCase = true)
    }

    // 从缓冲区读取字符串数据
    private fun readFromBuffer(buffer: Buffer, charset: Charset?): String {
        val bufferSize = buffer.size
        val maxBytes = min(bufferSize, maxContentLength)
        return StringBuilder().apply {
            try {
                append(buffer.readString(maxBytes, charset!!))
            } catch (e: EOFException) {
                append("\\n\\n--- Unexpected end of content ---")
            }
            if (bufferSize > maxContentLength) append("\\n\\n--- Content truncated ---")
        }.toString()
    }

}