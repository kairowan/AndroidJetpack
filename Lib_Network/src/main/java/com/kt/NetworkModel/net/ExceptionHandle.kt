package com.kt.network.net

import android.annotation.SuppressLint
import android.net.ParseException
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import com.kt.NetworkModel.helper.NetConfigHelper
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException

/**
 * @author 浩楠
 *
 * @date 2023/3/10-10:56.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO 根据不同的错误给出相应的提示
 */
@SuppressLint("StaticFieldLeak")
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
                NetConfigHelper.callback?.onToast("网络连接失败。请稍后再试")
                ResponseThrowable(ERROR.NETWORD_ERROR, e)
            }
            is javax.net.ssl.SSLException -> ResponseThrowable(ERROR.SSL_ERROR, e)
            is SocketTimeoutException -> {
                NetConfigHelper.callback?.onToast("网络连接失败。请稍后再试")
                ResponseThrowable(ERROR.TIMEOUT_ERROR, e)
            }
            is UnknownHostException -> {
                NetConfigHelper.callback?.onToast("网络连接失败。请稍后再试")
                ResponseThrowable(ERROR.NETWORD_ERROR, e)
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
                NetConfigHelper.callback?.onToast("网络地址错误。请稍后再试")
                ex = ResponseThrowable(ERROR.NOT_FOUND, e)
            }
            401, 403 -> {
                ex = ResponseThrowable(ERROR.TOKEN_EMPTY, e)
            }
            400 -> {
                ex = ResponseThrowable(ERROR.TOKEN_EMPTY, e)
            }
            else -> {
                ex = ResponseThrowable(ERROR.HTTP_ERROR, e)
            }
        }
        return ex
    }
}
