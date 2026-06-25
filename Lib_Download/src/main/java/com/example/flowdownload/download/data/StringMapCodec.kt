package com.example.flowdownload.download.data

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 字符串键值对编解码工具，用于在持久化层安全保存自定义元数据和请求头。
 */
object StringMapCodec {
    fun encode(values: Map<String, String>): String {
        if (values.isEmpty()) return ""
        return values.entries.joinToString(separator = "\n") { (key, value) ->
            "${key.urlEncode()}\t${value.urlEncode()}"
        }
    }

    fun decode(encoded: String): Map<String, String> {
        if (encoded.isBlank()) return emptyMap()
        return encoded.lineSequence()
            .filter { it.isNotBlank() && it.contains("\t") }
            .associate { line ->
                val separatorIndex = line.indexOf('\t')
                val key = line.substring(0, separatorIndex).urlDecode()
                val value = line.substring(separatorIndex + 1).urlDecode()
                key to value
            }
    }

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun String.urlDecode(): String =
        URLDecoder.decode(this, Charsets.UTF_8.name())
}
