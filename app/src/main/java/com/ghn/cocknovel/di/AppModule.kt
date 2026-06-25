package com.ghn.cocknovel.di

import com.kt.network.net.NetworkApiFactory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * 应用级 Koin 模块，负责注册整个宿主都会复用的基础依赖。
 */
@Module
@ComponentScan("com.ghn.cocknovel")
class AppModule {

    @Single
    fun provideNetworkApiFactory(): NetworkApiFactory {
        // 统一复用网络服务工厂，避免各 Repository 分散创建网络入口。
        return NetworkApiFactory()
    }
}
