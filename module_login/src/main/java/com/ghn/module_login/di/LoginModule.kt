package com.ghn.module_login.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * 登录功能的 Koin 模块入口，负责注册登录模块内的注解组件。
 */
@Module
@ComponentScan("com.ghn.module_login")
class LoginModule
