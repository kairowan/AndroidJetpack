package com.ghn.feature.capture.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * 抓包功能的 Koin 模块入口，负责将 feature 内被注解的组件注册进容器。
 */
@Module
@ComponentScan("com.ghn.feature.capture")
class CaptureModule
