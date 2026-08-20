package com.kotlinmvvm.data.feed.mapper

import com.kotlinmvvm.core.network.result.NetworkConnectionFailure
import com.kotlinmvvm.core.network.result.NetworkEmptyBodyFailure
import com.kotlinmvvm.core.network.result.NetworkEndpointFailure
import com.kotlinmvvm.core.network.result.NetworkFailure
import com.kotlinmvvm.core.network.result.NetworkHttpFailure
import com.kotlinmvvm.core.network.result.NetworkProtocolFailure
import com.kotlinmvvm.core.network.result.NetworkSecurityFailure
import com.kotlinmvvm.core.network.result.NetworkSerializationFailure
import com.kotlinmvvm.core.network.result.NetworkTimeoutFailure
import com.kotlinmvvm.core.network.result.NetworkUnexpectedFailure
import com.kotlinmvvm.domain.feed.result.FeedLoadError

/** 将基础网络失败收敛为 Feed 页面可稳定处理的业务错误。 */
internal fun NetworkFailure.toFeedError(): FeedLoadError = when (this) {
    NetworkConnectionFailure -> FeedLoadError.NO_CONNECTION
    NetworkTimeoutFailure -> FeedLoadError.TIMEOUT
    NetworkSerializationFailure,
    NetworkEmptyBodyFailure,
    NetworkEndpointFailure,
    NetworkSecurityFailure,
    NetworkProtocolFailure -> FeedLoadError.INVALID_RESPONSE

    is NetworkHttpFailure -> when {
        statusCode == 401 || statusCode == 403 -> FeedLoadError.UNAUTHORIZED
        statusCode >= 500 -> FeedLoadError.SERVER
        else -> FeedLoadError.UNKNOWN
    }

    NetworkUnexpectedFailure -> FeedLoadError.UNKNOWN
}
