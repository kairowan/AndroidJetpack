package com.kotlinmvvm.app

import android.app.Application
import com.kotlinmvvm.app.di.AppContainer
import com.kotlinmvvm.app.di.AppDependencies
import coil.ImageLoader
import coil.ImageLoaderFactory

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用进程入口，向所有 Activity 暴露可由手动容器或大型项目 DI 替换的依赖契约
 */
open class ComposeScaffoldApplication : Application(), ImageLoaderFactory {
    /** 进程内唯一依赖入口，具体装配方式不会泄漏给 Activity 和 Feature。 */
    val dependencies: AppDependencies by lazy {
        createDependencies()
    }

    /** 允许测试或大型项目 DI 仅替换依赖实现，不改变 Activity 与 Feature 契约。 */
    protected open fun createDependencies(): AppDependencies = AppContainer(applicationContext)

    override fun newImageLoader(): ImageLoader = dependencies.imageLoader
}
