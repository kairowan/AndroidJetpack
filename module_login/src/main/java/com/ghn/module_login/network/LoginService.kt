package com.ghn.module_login.network

import com.kt.network.bean.BaseResult
import com.kt.network.net.apiService

/**
 * 登录模块轻量网络入口：模块内统一发起登录相关请求。
 */
class LoginService {
    companion object {
        private const val HOST_MAIN = 1
    }
    suspend fun requestVerifyCode(phoneNumber: String): BaseResult<Any> {
        return apiService<LoginApiService>(HOST_MAIN)
            .requestVerifyCode(phoneNumber)
    }
}

