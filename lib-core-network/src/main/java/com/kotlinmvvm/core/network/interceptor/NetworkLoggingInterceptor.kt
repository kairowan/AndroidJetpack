package com.kotlinmvvm.core.network.interceptor

import java.io.File
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Debug 网络日志拦截器，清理中断残留并为 JSON 请求与响应安装不截断的旁路流式记录器
 */
class NetworkLoggingInterceptor(
    private val logger: NetworkLogger,
    private val formatter: NetworkLogFormatter,
    private val temporaryDirectory: File
) : Interceptor {

    init {
        removeStaleBodyFiles()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestRecorder = if (formatter.shouldStreamRequestBody(request)) {
            safeLog { logger.log(formatter.formatStreamingRequestStart(request)) }
            RecordingNetworkRequestBody(
                delegate = requireNotNull(request.body),
                temporaryDirectory = temporaryDirectory,
                onComplete = { reader ->
                    safeLog { formatter.logJsonRequestBody(reader, logger) }
                },
                onFailure = {
                    safeLog { formatter.logRequestBodyFailure(logger) }
                }
            )
        } else {
            safeLog { logger.log(formatter.formatRequestWithoutStreaming(request)) }
            null
        }
        val networkRequest = requestRecorder?.let { recorder ->
            request.newBuilder().method(request.method, recorder).build()
        } ?: request
        val startedAt = System.nanoTime()
        return try {
            val response = chain.proceed(networkRequest)
            requestRecorder?.failIfIncomplete()
            val elapsed = startedAt.elapsedMilliseconds()
            if (formatter.shouldStreamResponseBody(response)) {
                safeLog { logger.log(formatter.formatStreamingResponseStart(response, elapsed)) }
                response.newBuilder().body(
                    RecordingNetworkResponseBody(
                        delegate = response.body,
                        temporaryDirectory = temporaryDirectory,
                        onComplete = { reader ->
                            safeLog { formatter.logJsonResponseBody(reader, logger) }
                        },
                        onFailure = {
                            safeLog { formatter.logResponseBodyFailure(logger) }
                        }
                    )
                ).build()
            } else {
                safeLog { logger.log(formatter.formatResponseWithoutStreaming(response, elapsed)) }
                response
            }
        } catch (error: Exception) {
            requestRecorder?.failIfIncomplete()
            safeLog {
                logger.log(formatter.formatFailure(request, startedAt.elapsedMilliseconds(), error))
            }
            throw error
        }
    }

    private fun safeLog(action: () -> Unit) {
        try {
            action()
        } catch (_: Exception) {
            // 日志属于辅助能力，任何日志实现故障都不能中断真实网络请求。
        }
    }

    private fun removeStaleBodyFiles() {
        runCatching {
            temporaryDirectory.listFiles { file ->
                file.isFile && file.name.startsWith(NETWORK_LOG_FILE_PREFIX) &&
                    file.name.endsWith(NETWORK_LOG_FILE_SUFFIX)
            }.orEmpty().forEach(File::delete)
        }
    }

    private fun Long.elapsedMilliseconds(): Long =
        (System.nanoTime() - this) / NANOSECONDS_PER_MILLISECOND

    private companion object {
        const val NANOSECONDS_PER_MILLISECOND = 1_000_000L
    }
}
