package com.ghn.module_login.network

import com.kt.network.bean.BaseResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 登录模块专用接口定义。
 */
internal interface LoginApiService {

    @GET("api/user/auth/get/verifyCode")
    suspend fun requestVerifyCode(@Query("phoneNumber") phoneNumber: String): BaseResult<Any>
}
