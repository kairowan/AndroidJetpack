package com.example.flowdownload.download.data

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载重试退避时间计算器，统一生成指数退避的下一次尝试时间。
 */
internal object DownloadRetryBackoffCalculator {
    fun nextAttemptAtEpochMs(
        now: Long,
        retryCount: Int,
        baseBackoffMillis: Long,
    ): Long {
        val safeBase = baseBackoffMillis.coerceAtLeast(0L)
        if (safeBase == 0L) {
            return now
        }

        val exponent = retryCount.coerceIn(0, 62)
        val multiplier = 1L shl exponent
        val delayMillis = saturatingMultiply(safeBase, multiplier)
        return saturatingAdd(now, delayMillis)
    }

    fun remainingDelayMillis(
        now: Long,
        nextAttemptAtEpochMs: Long?,
    ): Long {
        val deadline = nextAttemptAtEpochMs ?: return 0L
        return (deadline - now).coerceAtLeast(0L)
    }

    private fun saturatingMultiply(left: Long, right: Long): Long {
        if (left == 0L || right == 0L) {
            return 0L
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE
        }
        return left * right
    }

    private fun saturatingAdd(left: Long, right: Long): Long {
        return if (Long.MAX_VALUE - left < right) {
            Long.MAX_VALUE
        } else {
            left + right
        }
    }
}
