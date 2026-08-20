package com.kotlinmvvm.core.network.interceptor

import java.io.File
import java.io.Reader
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import okio.sink

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Debug 请求正文记录器，在真实上传时旁路落盘并避免为完整 JSON 创建堆内副本
 */
internal class RecordingNetworkRequestBody(
    private val delegate: RequestBody,
    private val temporaryDirectory: File,
    private val onComplete: (Reader) -> Unit,
    private val onFailure: () -> Unit
) : RequestBody() {
    private var completed = false

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun isDuplex(): Boolean = delegate.isDuplex()

    override fun isOneShot(): Boolean = delegate.isOneShot()

    override fun writeTo(sink: BufferedSink) {
        val file = createTemporaryFile()
        var recorder = runCatching { file?.sink()?.buffer() }.getOrNull()
        if (file == null || recorder == null) {
            fail()
            delegate.writeTo(sink)
            return
        }

        var recordingFailed = false
        val recordingSink = object : ForwardingSink(sink) {
            override fun write(source: Buffer, byteCount: Long) {
                val target = recorder
                if (target != null) {
                    try {
                        source.copyTo(target.buffer, 0L, byteCount)
                        target.emitCompleteSegments()
                    } catch (_: Exception) {
                        recordingFailed = true
                        runCatching { target.close() }
                        recorder = null
                        file.delete()
                    }
                }
                super.write(source, byteCount)
            }
        }.buffer()

        try {
            delegate.writeTo(recordingSink)
            recordingSink.flush()
            recorder?.close()
            if (recordingFailed) {
                fail()
            } else {
                complete(file)
            }
        } catch (error: Exception) {
            runCatching { recorder?.close() }
            file.delete()
            fail()
            throw error
        }
    }

    @Synchronized
    fun failIfIncomplete() {
        if (!completed) fail()
    }

    private fun createTemporaryFile(): File? = runCatching {
        check(temporaryDirectory.exists() || temporaryDirectory.mkdirs())
        File.createTempFile(NETWORK_LOG_FILE_PREFIX, NETWORK_LOG_FILE_SUFFIX, temporaryDirectory)
    }.getOrNull()

    @Synchronized
    private fun complete(file: File) {
        if (completed) {
            file.delete()
            return
        }
        completed = true
        try {
            file.bufferedReader().use(onComplete)
        } catch (_: Exception) {
            onFailure()
        } finally {
            file.delete()
        }
    }

    @Synchronized
    private fun fail() {
        if (completed) return
        completed = true
        onFailure()
    }
}
