package com.ghn.module_login

import android.content.Context
import com.ghn.routermodule.LoginRouter
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.RouterParams
import com.ghn.routermodule.navigate
import com.therouter.inject.ServiceProvider
import com.therouter.inject.Singleton

@Singleton
@ServiceProvider(returnType = LoginRouter::class)
class LoginNavigatorProvider : LoginRouter {
    override fun openLogin(loginRequestId: String?, context: Context?) {
        RouterPath.Login.LoginAC.navigate(context) {
            loginRequestId?.takeIf { it.isNotBlank() }?.let {
                withString(RouterParams.KEY_LOGIN_REQUEST_ID, it)
            }
        }
    }
}
