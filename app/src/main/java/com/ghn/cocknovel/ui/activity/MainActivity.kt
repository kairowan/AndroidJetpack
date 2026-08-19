package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ghn.cocknovel.App
import com.ghn.cocknovel.ui.AppRoot

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
 * 描述: Android Compose 宿主，只负责接入进程依赖和根返回行为
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dependencies = (application as App).dependencies
        setContent {
            AppRoot(
                dependencies = dependencies,
                onRootBack = ::finish
            )
        }
    }
}
