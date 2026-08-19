package com.ghn.cocknovel.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ghn.cocknovel.di.AppDependencies
import com.ghn.cocknovel.navigation.AppNavHost
import com.kotlinmvvm.core.designsystem.theme.AppTheme

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
 * 描述: App 根组合，负责把应用依赖接入 Compose 壳层
 */
@Composable
fun AppRoot(
    dependencies: AppDependencies,
    onRootBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember(dependencies) { dependencies.eyepetizerRepository }
    val videoPlayerFactory = remember(dependencies) { dependencies.videoPlayerFactory }

    AppTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            AppNavHost(
                repository = repository,
                videoPlayerFactory = videoPlayerFactory,
                onRootBack = onRootBack
            )
        }
    }
}
