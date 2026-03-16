package com.ghn.module_login

import com.ghn.routermodule.LoginRouter
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.navigate
import com.therouter.inject.ServiceProvider
import com.therouter.inject.Singleton

@Singleton
@ServiceProvider(returnType = LoginRouter::class)
class LoginNavigatorProvider : LoginRouter {
    override fun openLogin() {
        RouterPath.Login.LoginAC.navigate()
    }
}
