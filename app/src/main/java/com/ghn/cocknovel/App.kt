package com.ghn.cocknovel

import android.app.Application
import com.ghn.cocknovel.di.AppContainer
import com.ghn.cocknovel.di.AppDependencies

/**
 * @author 浩楠
 *
 * @date 2026/7/24 12:05
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Android 进程入口，只暴露可替换的应用依赖
 */

class App : Application() {
    val dependencies: AppDependencies by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppContainer(this)
    }
}
