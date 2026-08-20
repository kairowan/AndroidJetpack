package com.kotlinmvvm.core.network.interceptor

import java.io.Reader
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 网络日志格式化器，完整流式输出脱敏 JSON 并仅按 Logcat 单条容量分段
 */
class NetworkLogFormatter(
    private val labels: NetworkLogLabels,
    private val logBodies: Boolean = true
) {
    /** 判断可重复读取的 JSON 请求是否应在真实上传过程中旁路流式记录。 */
    fun shouldStreamRequestBody(request: Request): Boolean {
        val body = request.body ?: return false
        return logBodies && body.contentLength() != 0L &&
            !body.isDuplex() && !body.isOneShot() && body.contentType()?.isJson() == true
    }

    /** 格式化流式 JSON 请求的头部；正文与底部由 [logJsonRequestBody] 继续输出。 */
    fun formatStreamingRequestStart(request: Request): String = formatBlockStart(
        title = labels.requestTitle,
        fields = requestFields(request),
        sections = listOf(labels.requestHeaders to formatHeaders(request.headers)),
        trailingSection = labels.requestBody
    )

    /** 格式化不需要流式读取的请求，包括空正文、表单、关闭正文日志和非 JSON 正文。 */
    fun formatRequestWithoutStreaming(request: Request): String = formatBlock(
        title = labels.requestTitle,
        fields = requestFields(request),
        sections = listOf(
            labels.requestHeaders to formatHeaders(request.headers),
            labels.requestBody to requestBodyLabel(request.body)
        )
    )

    /** 流式读取、脱敏并分段输出完整 JSON 请求，分段不会省略任何正文内容。 */
    fun logJsonRequestBody(reader: Reader, logger: NetworkLogger) = logJsonBody(reader, logger)

    /** 请求正文未写出或旁路记录失败时输出失败标记并闭合日志块。 */
    fun logRequestBodyFailure(logger: NetworkLogger) = logBodyFailure(logger)

    /** 判断响应是否需要由拦截器记录完整 JSON 正文后再流式输出。 */
    fun shouldStreamResponseBody(response: Response): Boolean {
        val body = response.body
        return logBodies && body.contentLength() != 0L &&
            body.contentType()?.isJson() == true
    }

    /** 格式化流式 JSON 响应的头部；正文与底部由 [logJsonResponseBody] 继续输出。 */
    fun formatStreamingResponseStart(response: Response, elapsedMilliseconds: Long): String =
        formatBlockStart(
            title = labels.responseTitle,
            fields = responseFields(response, elapsedMilliseconds),
            sections = listOf(labels.responseHeaders to formatHeaders(response.headers)),
            trailingSection = labels.responseBody
        )

    /** 流式读取、脱敏并分段输出完整 JSON，分段不会省略任何正文内容。 */
    fun logJsonResponseBody(reader: Reader, logger: NetworkLogger) {
        logJsonBody(reader, logger)
    }

    private fun logJsonBody(reader: Reader, logger: NetworkLogger) {
        try {
            StreamingJsonLogWriter(labels.redacted, logger).write(reader)
        } catch (_: Exception) {
            logger.log("│   ${labels.bodyReadFailed}")
        } finally {
            logger.log(BLOCK_END)
        }
    }

    /** 流式记录无法完成时输出明确失败标记并闭合日志块。 */
    fun logResponseBodyFailure(logger: NetworkLogger) {
        logBodyFailure(logger)
    }

    private fun logBodyFailure(logger: NetworkLogger) {
        logger.log("│   ${labels.bodyReadFailed}")
        logger.log(BLOCK_END)
    }

    /** 格式化不需要流式读取的响应，包括空正文、关闭正文日志和非 JSON 正文。 */
    fun formatResponseWithoutStreaming(response: Response, elapsedMilliseconds: Long): String =
        formatBlock(
            title = labels.responseTitle,
            fields = responseFields(response, elapsedMilliseconds),
            sections = listOf(
                labels.responseHeaders to formatHeaders(response.headers),
                labels.responseBody to responseBodyLabel(response)
            )
        )

    /** 格式化未获得响应时的请求摘要、耗时和异常类型，不输出异常消息中的潜在敏感数据。 */
    fun formatFailure(
        request: Request,
        elapsedMilliseconds: Long,
        error: Exception
    ): String = formatBlock(
        title = labels.failureTitle,
        fields = requestFields(request) + listOf(
            labels.duration to "$elapsedMilliseconds ${labels.millisecondUnit}",
            labels.exception to error.javaClass.simpleName
        )
    )

    private fun requestFields(request: Request) = listOf(
        labels.requestId to request.header(HEADER_REQUEST_ID).orEmpty().ifBlank { labels.empty },
        labels.method to request.method,
        labels.url to redactUrl(request.url)
    )

    private fun responseFields(response: Response, elapsedMilliseconds: Long) = listOf(
        labels.requestId to response.request.header(HEADER_REQUEST_ID).orEmpty()
            .ifBlank { labels.empty },
        labels.method to response.request.method,
        labels.url to redactUrl(response.request.url),
        labels.status to listOf(response.code.toString(), response.message)
            .filter(String::isNotBlank)
            .joinToString(" "),
        labels.duration to "$elapsedMilliseconds ${labels.millisecondUnit}"
    )

    private fun requestBodyLabel(body: RequestBody?): String {
        if (!logBodies) return labels.bodyDisabled
        if (body == null) return labels.empty
        if (body.contentLength() == 0L) return labels.empty
        if (body.isDuplex() || body.isOneShot()) return labels.oneShotBodyOmitted
        if (body is FormBody) return formatFormBody(body)
        return if (body.contentType()?.isJson() == true) labels.bodyReadFailed
        else body.contentType().omittedBodyLabel()
    }

    private fun responseBodyLabel(response: Response): String {
        if (!logBodies) return labels.bodyDisabled
        val body = response.body
        if (body.contentLength() == 0L) return labels.empty
        return if (body.contentType()?.isJson() == true) {
            labels.bodyReadFailed
        } else {
            body.contentType().omittedBodyLabel()
        }
    }

    private fun formatFormBody(body: FormBody): String = (0 until body.size).joinToString("&") { index ->
        val name = body.name(index)
        val value = if (NetworkLogRedactionPolicy.isSensitiveName(name)) {
            labels.redacted
        } else {
            body.value(index)
        }
        "$name=$value"
    }.ifBlank { labels.empty }

    private fun formatHeaders(headers: Headers): String {
        if (headers.size == 0) return labels.empty
        return headers.names().flatMap { name ->
            headers.values(name).map { value ->
                "$name: ${
                    if (NetworkLogRedactionPolicy.isSensitiveHeader(name)) labels.redacted else value
                }"
            }
        }.joinToString("\n")
    }

    private fun redactUrl(url: HttpUrl): String = url.newBuilder().apply {
        url.queryParameterNames.filter(NetworkLogRedactionPolicy::isSensitiveName).forEach { name ->
            setQueryParameter(name, labels.redacted)
        }
    }.build().toString()

    private fun MediaType.isJson(): Boolean = subtype.contains(JSON, ignoreCase = true)

    private fun MediaType?.omittedBodyLabel(): String = if (this?.type.equals(TEXT, true)) {
        labels.unstructuredBodyOmitted
    } else {
        labels.binaryBodyOmitted
    }

    private fun formatBlock(
        title: String,
        fields: List<Pair<String, String>>,
        sections: List<Pair<String, String>> = emptyList()
    ): String = formatBlockStart(title, fields, sections) + "\n$BLOCK_END"

    private fun formatBlockStart(
        title: String,
        fields: List<Pair<String, String>>,
        sections: List<Pair<String, String>> = emptyList(),
        trailingSection: String? = null
    ): String = buildString {
        append("┌──────── ").append(title).append(" ────────")
        fields.forEach { (name, value) -> append("\n│ ").append(name).append(": ").append(value) }
        sections.forEach { (name, value) ->
            append("\n│ ").append(name).append(":")
            value.lineSequence().forEach { append("\n│   ").append(it) }
        }
        trailingSection?.let { append("\n│ ").append(it).append(":") }
    }

    private companion object {
        const val HEADER_REQUEST_ID = "X-Request-ID"
        const val JSON = "json"
        const val TEXT = "text"
        const val BLOCK_END = "└────────────────────────"
    }
}
