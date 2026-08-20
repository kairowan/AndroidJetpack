package com.kotlinmvvm.core.network.stream

import okio.BufferedSource

/**
 * @author 浩楠
 * @date 2026/8/20
 * 描述: 按 SSE 协议逐行解析字段并维护跨事件持续生效的最后事件编号
 */
internal class SseParser(
    initialLastEventId: String?,
    private val emit: (SseEvent) -> Unit
) {
    private var lastEventId = initialLastEventId
    private var eventType: String? = null
    private val data = mutableListOf<String>()

    /** 消费 UTF-8 响应流，只有空行完成一组字段时才派发业务消息。 */
    fun parse(source: BufferedSource) {
        var firstLine = true
        while (true) {
            var line = source.readUtf8Line() ?: return
            if (firstLine) {
                line = line.removePrefix(UTF8_BOM)
                firstLine = false
            }
            when {
                line.isEmpty() -> dispatchMessage()
                line.startsWith(':') -> Unit
                else -> parseField(line)
            }
        }
    }

    private fun parseField(line: String) {
        val separator = line.indexOf(':')
        val field = if (separator < 0) line else line.substring(0, separator)
        val rawValue = if (separator < 0) "" else line.substring(separator + 1)
        val value = rawValue.removePrefix(" ")
        when (field) {
            "data" -> data += value
            "event" -> eventType = value
            "id" -> if ('\u0000' !in value) lastEventId = value
            "retry" -> value.takeIf { it.isAsciiDigits() }
                ?.toLongOrNull()
                ?.let { emit(SseRetry(it)) }
        }
    }

    private fun dispatchMessage() {
        if (data.isNotEmpty()) {
            emit(
                SseMessage(
                    data = data.joinToString("\n"),
                    id = lastEventId,
                    type = eventType?.takeIf(String::isNotEmpty) ?: SseEvent.DEFAULT_EVENT_TYPE
                )
            )
        }
        data.clear()
        eventType = null
    }

    private fun String.isAsciiDigits(): Boolean =
        isNotEmpty() && all { it in '0'..'9' }

    private companion object {
        const val UTF8_BOM = "\uFEFF"
    }
}
