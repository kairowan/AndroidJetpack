package com.kotlinmvvm.core.network.interceptor

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用网络日志分段装饰器，确保请求头、请求体和异常日志也不会被平台单条容量截断
 */
class SegmentedNetworkLogger(
    private val delegate: NetworkLogger,
    private val segmentCharacters: Int = DEFAULT_SEGMENT_CHARACTERS
) : NetworkLogger {
    init {
        require(segmentCharacters >= 2)
    }

    override fun log(message: String) {
        if (message.isEmpty()) {
            delegate.log(message)
            return
        }
        var start = 0
        while (start < message.length) {
            var end = (start + segmentCharacters).coerceAtMost(message.length)
            if (end < message.length && message[end - 1].isHighSurrogate() &&
                message[end].isLowSurrogate()
            ) {
                end--
            }
            delegate.log(message.substring(start, end))
            start = end
        }
    }

    private companion object {
        const val DEFAULT_SEGMENT_CHARACTERS = 3_500
    }
}
