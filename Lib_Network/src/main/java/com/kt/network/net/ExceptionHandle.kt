package com.kt.network.net

import android.net.ParseException
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException

object ExceptionHandle {
    fun handleException(e: Throwable): ResponseThrowable {
        if (e is CancellationException) {
            throw e
        }
        val ex: ResponseThrowable = when (e) {
            is ResponseThrowable -> e
            is HttpException -> handleHttpException(e)
            is JsonParseException, is JSONException, is ParseException, is MalformedJsonException -> {
                ResponseThrowable(ERROR.PARSE_ERROR, e)
            }
            is ConnectException -> {
                ResponseThrowable(ERROR.NETWORK_ERROR, e)
            }
            is javax.net.ssl.SSLException -> ResponseThrowable(ERROR.SSL_ERROR, e)
            is SocketTimeoutException -> {
                ResponseThrowable(ERROR.TIMEOUT_ERROR, e)
            }
            is UnknownHostException -> {
                ResponseThrowable(ERROR.NETWORK_ERROR, e)
            }
            else -> {
                if (!e.message.isNullOrEmpty()) ResponseThrowable(1000, e.message!!, e)
                else ResponseThrowable(ERROR.UNKNOWN, e)
            }
        }
        return ex
    }

    private fun handleHttpException(e: HttpException): ResponseThrowable {
        val ex: ResponseThrowable
        when (e.code()) {
            404 -> {
                ex = ResponseThrowable(ERROR.NOT_FOUND, e)
            }
            401, 403 -> {
                ex = ResponseThrowable(ERROR.UNAUTHORIZED, e)
            }
            400 -> {
                ex = ResponseThrowable(ERROR.BAD_REQUEST, e)
            }
            else -> {
                ex = ResponseThrowable(ERROR.HTTP_ERROR, e)
            }
        }
        return ex
    }
}
