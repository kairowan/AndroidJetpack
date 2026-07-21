package com.kotlinmvvm.core.network.exception

import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import com.kotlinmvvm.core.network.result.NetworkFailure
import com.kotlinmvvm.core.network.result.NetworkResult
import com.kotlinmvvm.core.network.result.NetworkConnectionFailure
import com.kotlinmvvm.core.network.result.NetworkEmptyBodyFailure
import com.kotlinmvvm.core.network.result.NetworkEndpointFailure
import com.kotlinmvvm.core.network.result.NetworkError
import com.kotlinmvvm.core.network.result.NetworkHttpFailure
import com.kotlinmvvm.core.network.result.NetworkSerializationFailure
import com.kotlinmvvm.core.network.result.NetworkSecurityFailure
import com.kotlinmvvm.core.network.result.NetworkProtocolFailure
import com.kotlinmvvm.core.network.result.NetworkSuccess
import com.kotlinmvvm.core.network.result.NetworkTimeoutFailure
import com.kotlinmvvm.core.network.result.NetworkUnexpectedFailure
import com.kotlinmvvm.core.network.interceptor.UnregisteredNetworkEndpointException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.ProtocolException
import java.security.cert.CertificateException
import javax.net.ssl.SSLException

/**
 * @author 浩楠
 * @date 2026/7/20 13:28
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 网络边界统一异常处理器，集中转换 Retrofit 响应和传输异常为稳定的 NetworkFailure
 */
class NetworkExceptionHandler {

    /** 将 Retrofit 响应统一转换为成功结果、HTTP 失败或空正文失败。 */
    fun <T : Any> handleResponse(response: Response<T>): NetworkResult<T> {
        if (!response.isSuccessful) return NetworkError(response.toHttpFailure())
        return response.body()
            ?.let(::NetworkSuccess)
            ?: NetworkError(NetworkEmptyBodyFailure)
    }

    /** 将请求执行异常归类为稳定失败类型；协程取消异常必须由调用方提前重新抛出。 */
    fun handleException(error: Exception): NetworkFailure = when (error) {
        is UnregisteredNetworkEndpointException -> NetworkEndpointFailure
        is SocketTimeoutException -> NetworkTimeoutFailure
        is UnknownHostException, is ConnectException -> NetworkConnectionFailure
        else -> when {
            error.hasSecurityCause() -> NetworkSecurityFailure
            error.hasProtocolCause() -> NetworkProtocolFailure
            error.hasSerializationCause() -> NetworkSerializationFailure
            error is HttpException -> error.response()?.toHttpFailure()
                ?: NetworkHttpFailure(error.code())
            error is IOException -> NetworkConnectionFailure
            else -> NetworkUnexpectedFailure
        }
    }

    private fun Throwable.hasSerializationCause(): Boolean = generateSequence(this) { it.cause }
        .any { it is JsonParseException || it is MalformedJsonException }

    private fun Throwable.hasSecurityCause(): Boolean = generateSequence(this) { it.cause }
        .any { it is SSLException || it is CertificateException }

    private fun Throwable.hasProtocolCause(): Boolean = generateSequence(this) { it.cause }
        .any { it is ProtocolException }

    private fun Response<*>.toHttpFailure() = NetworkHttpFailure(
        statusCode = code()
    )
}
