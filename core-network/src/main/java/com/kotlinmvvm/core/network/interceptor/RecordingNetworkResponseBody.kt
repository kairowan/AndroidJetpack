package com.kotlinmvvm.core.network.interceptor

import java.io.File
import java.io.Reader
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer
import okio.sink

internal const val NETWORK_LOG_FILE_PREFIX = "network-response-"
internal const val NETWORK_LOG_FILE_SUFFIX = ".json"

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Debug 响应正文记录器，在应用正常消费响应时旁路落盘并于 EOF 触发完整流式日志
 */
internal class RecordingNetworkResponseBody(
    private val delegate: ResponseBody,
    temporaryDirectory: File,
    private val onComplete: (Reader) -> Unit,
    private val onFailure: () -> Unit
) : ResponseBody() {
    private val temporaryFile = runCatching {
        check(temporaryDirectory.exists() || temporaryDirectory.mkdirs())
        File.createTempFile(NETWORK_LOG_FILE_PREFIX, NETWORK_LOG_FILE_SUFFIX, temporaryDirectory)
    }.getOrNull()
    private var recorder = runCatching { temporaryFile?.sink()?.buffer() }.getOrNull()
    private var recordingFailed = temporaryFile == null || recorder == null
    private var completed = false

    private val recordingSource: BufferedSource by lazy {
        object : ForwardingSource(delegate.source()) {
            override fun read(sink: Buffer, byteCount: Long): Long {
                val startOffset = sink.size
                val read = super.read(sink, byteCount)
                if (read > 0L) record(sink, startOffset, read)
                if (read == -1L) complete()
                return read
            }

            override fun close() {
                try {
                    super.close()
                } finally {
                    if (!completed) fail()
                }
            }
        }.buffer()
    }

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun source(): BufferedSource = recordingSource

    private fun record(buffer: Buffer, offset: Long, byteCount: Long) {
        val target = recorder ?: return
        try {
            buffer.copyTo(target.buffer, offset, byteCount)
            target.emitCompleteSegments()
        } catch (_: Exception) {
            recordingFailed = true
            runCatching { target.close() }
            recorder = null
            temporaryFile?.delete()
        }
    }

    private fun complete() {
        if (completed) return
        completed = true
        val file = temporaryFile
        try {
            recorder?.close()
            if (recordingFailed || file == null) {
                onFailure()
            } else {
                file.bufferedReader().use(onComplete)
            }
        } catch (_: Exception) {
            onFailure()
        } finally {
            file?.delete()
        }
    }

    private fun fail() {
        if (completed) return
        completed = true
        runCatching { recorder?.close() }
        temporaryFile?.delete()
        onFailure()
    }
}
