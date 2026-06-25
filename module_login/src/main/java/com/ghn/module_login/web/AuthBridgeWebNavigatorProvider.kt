package com.ghn.module_login.web

import com.ghn.routermodule.AuthBridgeWebRouter
import com.therouter.inject.ServiceProvider
import com.therouter.inject.Singleton

@Singleton
@ServiceProvider(returnType = AuthBridgeWebRouter::class)
class AuthBridgeWebNavigatorProvider : AuthBridgeWebRouter
