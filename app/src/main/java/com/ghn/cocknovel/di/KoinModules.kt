package com.ghn.cocknovel.di

import com.ghn.module_login.di.LoginModule
import org.koin.ksp.generated.*

val appModules = listOf(
    LoginModule().module,
    AppModule().module
)
