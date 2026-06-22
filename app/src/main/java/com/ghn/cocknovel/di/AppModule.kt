package com.ghn.cocknovel.di

import com.kt.network.net.NetworkApiFactory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.ghn.cocknovel")
class AppModule {

    @Single
    fun provideNetworkApiFactory(): NetworkApiFactory = NetworkApiFactory()
}
