package com.kotlinmvvm.core.player.engine

import androidx.media3.common.PlaybackException
import com.kotlinmvvm.core.player.api.PlayerFailure

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 将 Media3 错误码压缩为稳定播放器失败类别，避免引擎细节泄漏到业务页面
 */
internal fun PlaybackException.toPlayerFailure(): PlayerFailure = when {
    errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
        errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
        PlayerFailure.NETWORK

    errorCode in 2_000..3_999 -> PlayerFailure.SOURCE
    errorCode in 4_000..4_999 -> PlayerFailure.DECODING
    errorCode in 5_000..7_999 -> PlayerFailure.RENDERING
    else -> PlayerFailure.UNKNOWN
}
