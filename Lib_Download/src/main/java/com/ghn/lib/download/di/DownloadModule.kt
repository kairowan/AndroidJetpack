package com.ghn.lib.download.di

import com.ghn.lib.download.DownloadService
import com.ghn.lib.download.FlowDownloadService
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * @author 浩楠
 *
 * @date 2026/6/24
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 下载模块依赖定义，负责向容器注册下载相关服务。
 */

val downloadModule: Module = module {
    // 对外只暴露 DownloadService 接口，便于宿主层按抽象依赖下载能力。
    single<DownloadService> { FlowDownloadService(get()) }
}
