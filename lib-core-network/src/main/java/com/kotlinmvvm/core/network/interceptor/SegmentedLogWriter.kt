package com.kotlinmvvm.core.network.interceptor

import java.io.Writer

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 网络正文分段日志写入器，仅规避 Logcat 单条长度限制且不截断完整正文
 */
internal class SegmentedLogWriter(
    private val logger: NetworkLogger
) : Writer() {
    private val segment = StringBuilder()
    private var linePrefixRequired = true

    override fun write(buffer: CharArray, offset: Int, length: Int) {
        for (index in offset until offset + length) {
            if (linePrefixRequired) {
                segment.append(LOG_LINE_PREFIX)
                linePrefixRequired = false
            }
            val character = buffer[index]
            segment.append(character)
            if (character == '\n') linePrefixRequired = true
            if (segment.length >= LOG_SEGMENT_CHARACTERS) flushSegment()
        }
    }

    override fun flush() = flushSegment()

    override fun close() = flushSegment()

    private fun flushSegment() {
        if (segment.isEmpty()) return
        logger.log(segment.toString().trimEnd('\n'))
        segment.clear()
        linePrefixRequired = true
    }

    private companion object {
        const val LOG_LINE_PREFIX = "│   "
        const val LOG_SEGMENT_CHARACTERS = 3_500
    }
}
