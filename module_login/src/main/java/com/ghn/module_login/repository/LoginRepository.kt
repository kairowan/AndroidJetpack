package com.ghn.module_login.repository

import com.ghn.lib.base.aop.TraceTime
import com.ghn.module_login.network.LoginApiService
import com.kt.network.net.NetworkApiFactory
import org.koin.core.annotation.Single

/**
 * @author 浩楠
 *
 * @date 2026/5/18
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: 登录模块数据仓库，统一封装登录相关接口调用。
 */
@Single
class LoginRepository(
    networkApiFactory: NetworkApiFactory
) {

    private val loginApiService: LoginApiService = networkApiFactory.get()

    @TraceTime("login_request_verify_code", warnAtMillis = 120L)
    suspend fun requestVerifyCode(phoneNumber: String) =
        loginApiService.requestVerifyCode(phoneNumber)
}
