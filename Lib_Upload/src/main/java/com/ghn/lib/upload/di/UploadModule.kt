package com.ghn.lib.upload.di

import com.ghn.lib.upload.FlowUploadService
import com.ghn.lib.upload.UploadService
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
 * 描述: 上传模块依赖定义，负责向容器注册上传相关服务。
 */

val uploadModule: Module = module {
    // 通过接口绑定隐藏具体实现，方便后续替换上传引擎或做测试桩。
    single<UploadService> { FlowUploadService(get()) }
}
