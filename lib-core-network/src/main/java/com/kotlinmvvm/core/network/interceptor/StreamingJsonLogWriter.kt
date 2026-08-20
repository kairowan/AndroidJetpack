package com.kotlinmvvm.core.network.interceptor

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.Reader

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: JSON 正文流日志写入器，边读取边脱敏并按 Logcat 单条容量分段且不省略请求或响应内容
 */
internal class StreamingJsonLogWriter(
    private val redactedValue: String,
    private val logger: NetworkLogger
) {
    fun write(reader: Reader) {
        JsonReader(reader).use { jsonReader ->
            JsonWriter(SegmentedLogWriter(logger)).use { jsonWriter ->
                jsonWriter.setIndent("  ")
                copyJson(jsonReader, jsonWriter)
            }
        }
    }

    private fun copyJson(reader: JsonReader, writer: JsonWriter) {
        when (reader.peek()) {
            JsonToken.BEGIN_OBJECT -> {
                reader.beginObject()
                writer.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    writer.name(name)
                    if (NetworkLogRedactionPolicy.isSensitiveName(name)) {
                        reader.skipValue()
                        writer.value(redactedValue)
                    } else {
                        copyJson(reader, writer)
                    }
                }
                reader.endObject()
                writer.endObject()
            }

            JsonToken.BEGIN_ARRAY -> {
                reader.beginArray()
                writer.beginArray()
                while (reader.hasNext()) copyJson(reader, writer)
                reader.endArray()
                writer.endArray()
            }

            JsonToken.STRING -> writer.value(reader.nextString())
            JsonToken.NUMBER -> writer.jsonValue(reader.nextString())
            JsonToken.BOOLEAN -> writer.value(reader.nextBoolean())
            JsonToken.NULL -> {
                reader.nextNull()
                writer.nullValue()
            }

            JsonToken.END_DOCUMENT -> Unit
            else -> reader.skipValue()
        }
    }

}
