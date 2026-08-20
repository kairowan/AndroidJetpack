package com.kotlinmvvm.app.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kotlinmvvm.app.ComposeScaffoldApplication
import com.kotlinmvvm.app.navigation.AppNavHost
import com.kotlinmvvm.app.window.ActivityVideoWindowController
import com.kotlinmvvm.core.designsystem.theme.AppTheme

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 主页面 Activity，承载应用多返回栈、根页面退出与单 Activity 模式窗口策略
 */
class MainActivity : ComponentActivity() {
    private lateinit var videoWindowController: ActivityVideoWindowController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        videoWindowController = ActivityVideoWindowController(this)
        val dependencies = (application as ComposeScaffoldApplication).dependencies
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(
                        pageRepository = dependencies.feedPageRepository,
                        videoRepository = dependencies.feedVideoRepository,
                        videoPlayerFactory = dependencies.videoPlayerFactory,
                        taskObserver = dependencies.viewModelTaskObserver,
                        navigationMode = dependencies.navigationMode,
                        onOpenVideoActivity = { arguments ->
                            startActivity(VideoDetailActivity.createIntent(this, arguments))
                        },
                        onWindowModeChanged = videoWindowController::apply,
                        onRootBack = this@MainActivity::finish
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) videoWindowController.restore()
        super.onDestroy()
    }
}
