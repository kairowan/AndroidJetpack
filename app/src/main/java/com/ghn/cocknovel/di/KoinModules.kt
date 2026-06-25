package com.ghn.cocknovel.di

import com.ghn.feature.capture.di.CaptureModule
import com.ghn.lib.download.di.downloadModule
import com.ghn.lib.upload.di.uploadModule
import com.ghn.module_login.di.LoginModule
import org.koin.ksp.generated.*

/**
 * 汇总应用启动时需要装载的全部 Koin 模块。
 */
val appModules = listOf(
    // KSP 扫描生成的业务模块定义。
    CaptureModule().module,
    LoginModule().module,
    // 手写模块用于补充接口到实现的绑定关系。
    downloadModule,
    uploadModule,
    // 应用级基础设施依赖统一放在最后集中注册。
    AppModule().module
)
