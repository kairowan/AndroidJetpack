package com.ghn.feature.capture.utils

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.ghn.feature.capture.R
import com.ghn.feature.capture.ui.widget.LongClickCopyTextView

import okhttp3.Headers
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.StatusLine
import okio.Buffer
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.net.HttpURLConnection
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.xml.transform.OutputKeys
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult

/**
 * Author: zpj
 * Date: 2023-09-05 15:13
 * Desc: okhttp相关扩展
 */
/**
 * HTTP请求/响应头实体类
 * */
data class HttpHeader(val name: String, val value: String)

/**
 * 快速生成显示请求/响应头的控件
 * */
internal fun HttpHeader.generateHeaderView(context: Context) = LinearLayout(context).apply {
    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        .apply { setMargins(0, 8.dp, 0, 0) }
    orientation = LinearLayout.VERTICAL
    addView(LongClickCopyTextView(context).apply {
        text = SpannableString("${name}: $value").apply {
            setSpan(
                ForegroundColorSpan(context.getColorRes(R.color.font_v2)),
                0, name.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.BOLD),
                0, name.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(context.getColorRes(R.color.font_v1)),
                name.length + 1, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    })
    addView(View(context).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
            .apply { setMargins(0, 8.dp, 0, 0) }
        setBackgroundColor(context.getColorRes(R.color.gray_line))
    })
}

/**
 * 请求/响应头转Json字符串
 * */
fun Headers?.toJsonString() = if (this != null && this.size > 0)
    GsonHelper.toJson((0 until this.size).map { HttpHeader(this.name(it), this.value(it)) }) else ""

/**
 * 判断是否支持获取响应体
 * */
fun Response.promisesBody(): Boolean {
    if (request.method == "HEAD") return false
    code.let { responseCode ->
        if ((responseCode < StatusLine.HTTP_CONTINUE || responseCode >= 200)
            && responseCode != HttpURLConnection.HTTP_NO_CONTENT
            && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED
        ) return true
    }
    if (headersContentLength() != -1L || "chunked".equals(header("Transfer-Encoding"), ignoreCase = true)) return true
    return false
}

/**
 * 获取响应头内容长度
 * */
fun Response.headersContentLength(): Long {
    return headers["Content-Length"]?.toLongOrNull() ?: -1L
}

/**
 * 判断Buffer对象中的数据是否可能为UTF-8编码
 * */
internal fun Buffer.isProbablyUtf8(): Boolean {
    try {
        val prefix = Buffer()
        val byteCount = size.coerceAtMost(64)
        copyTo(prefix, 0, byteCount)
        for (i in 0 until 16) {
            if (prefix.exhausted()) break
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) return false
        }
        return true
    } catch (_: EOFException) {
        return false // Truncated UTF-8 sequence.
    }
}

/**
 * 读取请求头内容
 * */
@RequiresApi(Build.VERSION_CODES.KITKAT)
fun RequestBody.readString(): String {
    var result = ""
    try {
        val buffer = Buffer()
        this.writeTo(buffer)
        val charset: Charset =
            this.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
        if (buffer.isProbablyUtf8()) {
            result = buffer.readString(charset)
        }
    } catch (e: Exception) {

    }
    return result
}

/**
 * 格式化响应体
 * */
@RequiresApi(Build.VERSION_CODES.FROYO)
fun formatBody(body: String, contentType: String?): String {
    return when {
        contentType?.contains("json", true) == true -> formatJson(body)
        contentType?.contains("xml", true) == true -> formatXml(body)
        else -> body
    }
}

/**
 * 格式化Json字符串
 * */
private fun formatJson(json: String) = GsonHelper.setPrettyPrinting(json)

/**
 * 格式化XML
 * */
@RequiresApi(Build.VERSION_CODES.FROYO)
private fun formatXml(xml: String): String {
    return try {
        val serializer = SAXTransformerFactory.newInstance().newTransformer()
        serializer.setOutputProperty(OutputKeys.INDENT, "yes")
        serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        val xmlSource = SAXSource(InputSource(ByteArrayInputStream(xml.toByteArray())))
        val res = StreamResult(ByteArrayOutputStream())
        serializer.transform(xmlSource, res)
        String((res.outputStream as ByteArrayOutputStream).toByteArray())
    } catch (e: Exception) {
        xml
    }
}