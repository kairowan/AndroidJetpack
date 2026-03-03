package com.kt.NetworkModel.net.interceptor

import com.kt.NetworkModel.helper.LogJsonUtils
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okio.Buffer
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

/**
 * @author 浩楠
 *
 * @date 2026-2-19
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

/**
 * @author 浩楠
 *
 * @date 2023/5/12-13:37.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO 对日志拦截器的格式进行处理
 */
object Printer {
    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"
    private val requestCounter = AtomicLong(1)

    fun generateId(): String = "[Req:${requestCounter.getAndIncrement()}]"

    private const val TOP_BORDER =
        "╔═══════════════════════════════════════════════════════════════════════════════════════"
    private const val BOTTOM_BORDER =
        "╚═══════════════════════════════════════════════════════════════════════════════════════"
    private const val VERTICAL_LINE = "║ "

    private fun printLog(
        builder: LoggingInterceptor,
        id: String,
        type: String,
        lines: List<String>
    ) {
        val tag = if (type == "Request") builder.requestTag else builder.responseTag
        val sb = StringBuilder()

        // 拼接头部
        sb.append(id).append(" ").append(TOP_BORDER).append(" ").append(type).append(LINE_SEPARATOR)

        // 拼接内容
        lines.forEach { line ->
            sb.append(id).append(" ").append(VERTICAL_LINE).append(line).append(LINE_SEPARATOR)
        }

        // 拼接底部
        sb.append(id).append(" ").append(BOTTOM_BORDER)

        val finalMsg = sb.toString()

        if (finalMsg.length <= 4000) {
            log(builder, tag, finalMsg)
        } else {
            var i = 0
            while (i < finalMsg.length) {
                val end = min(i + 4000, finalMsg.length)
                log(builder, tag, finalMsg.substring(i, end))
                i = end
            }
        }
    }

    private fun log(builder: LoggingInterceptor, tag: String, msg: String) {
        if (builder.logger != null) {
            builder.logger!!.log(builder.type, tag, msg)
        } else {
            LoggingInterceptor.Logger.DEFAULT.log(builder.type, tag, msg)
        }
    }

    internal fun printJsonRequest(builder: LoggingInterceptor, request: Request, id: String) {
        val lines = ArrayList<String>()
        lines.add("URL: ${request.url}")
        lines.add("Method: @${request.method}")
        appendHeaders(lines, builder, request.headers)
//        if (builder.level == Level.HEADERS || builder.level == Level.BODY || builder.level == Level.BASIC) {
//            lines.add("Headers:")
//            lines.addAll(formatHeaders(request.headers.toString()))
//        }

        if (request.body is FormBody) {
            val formBody = StringBuilder()
            val body = request.body as FormBody?
            if (body != null && body.size != 0) {
                for (i in 0 until body.size) {
                    formBody.append(body.encodedName(i) + "=" + body.encodedValue(i) + "&")
                }
                formBody.delete(formBody.length - 1, formBody.length)
                lines.add("FormBody: $formBody")
            }
        }

        if (builder.level == Level.BASIC || builder.level == Level.BODY) {
            val bodyString = bodyToString(request)
            if (bodyString.isNotEmpty()) {
                lines.add("Body:")
                lines.addAll(bodyString.split(LINE_SEPARATOR).filter { it.isNotBlank() })
            }
        }
        printLog(builder, id, "Request", lines)
    }

    internal fun printFileRequest(builder: LoggingInterceptor, request: Request, id: String) {
        val lines = ArrayList<String>()
        lines.add("URL: ${request.url}")
        lines.add("Method: @${request.method}")
        appendHeaders(lines, builder, request.headers)

        lines.add("Omitted request body (File)")
        printLog(builder, id, "Request", lines)
    }

    internal fun printJsonResponse(
        builder: LoggingInterceptor, chainMs: Long, isSuccessful: Boolean,
        code: Int, headers: String, bodyString: String, id: String
    ) {
        val lines = ArrayList<String>()
        lines.add("Result: Success=$isSuccessful  Time=${chainMs}ms  Code=$code")

//        if (builder.level == Level.HEADERS || builder.level == Level.BODY || builder.level == Level.BASIC) {
//            lines.add("Headers:")
//            lines.addAll(formatHeaders(headers))
//        }
        if (builder.level == Level.HEADERS || builder.level == Level.BASIC || builder.level == Level.BODY) {
            lines.add("Headers:")
            lines.addAll(formatHeadersFromString(headers))
        }

        if (builder.level == Level.BASIC || builder.level == Level.BODY) {
            lines.add("Body:")
            val json = LogJsonUtils.formatJson(bodyString)
            lines.addAll(json.split(LINE_SEPARATOR).filter { it.isNotBlank() })
        }
        printLog(builder, id, "Response", lines)
    }

    internal fun printFileResponse(
        builder: LoggingInterceptor, chainMs: Long, isSuccessful: Boolean,
        code: Int, id: String
    ) {
        val lines = ArrayList<String>()
        lines.add("Result: Success=$isSuccessful  Time=${chainMs}ms  Code=$code")
        lines.add("Omitted response body (File)")
        printLog(builder, id, "Response", lines)
    }

    private fun formatHeaders(header: String): List<String> {
        if (header.isBlank()) return emptyList()
        return header.split(LINE_SEPARATOR).filter { it.isNotBlank() }.map { "  $it" }
    }

    private fun appendHeaders(lines: ArrayList<String>, builder: LoggingInterceptor, headers: Headers) {
        if (builder.level == Level.HEADERS || builder.level == Level.BASIC || builder.level == Level.BODY) {
            lines.add("Headers:")
            for (i in 0 until headers.size) {
                lines.add("  ${headers.name(i)}: ${headers.value(i)}")
            }
        }
    }

    private fun formatHeadersFromString(header: String): List<String> {
        if (header.isBlank()) return emptyList()
        return header.split(LINE_SEPARATOR).filter { it.isNotBlank() }.map { "  ${it.trim()}" }
    }

    private fun bodyToString(request: Request): String {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            if (copy.body == null) return ""
            copy.body!!.writeTo(buffer)
            LogJsonUtils.formatJson(buffer.readUtf8())
        } catch (e: IOException) {
            "{\"err\": \"${e.message}\"}"
        }
    }
}
