package com.kotlinmvvm.app.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kotlinmvvm.app.ComposeScaffoldApplication
import com.kotlinmvvm.app.navigation.model.VideoRouteArguments
import com.kotlinmvvm.app.window.ActivityVideoWindowController
import com.kotlinmvvm.core.designsystem.theme.AppTheme
import com.kotlinmvvm.feature.detail.navigation.VideoDetailRoute

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 多 Activity 视频详情宿主，校验最小参数并向 Route 提供应用级视频仓库角色
 */
class VideoDetailActivity : ComponentActivity() {
    private lateinit var videoWindowController: ActivityVideoWindowController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = VideoRouteArguments.from(intent.extras)
        if (arguments == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        videoWindowController = ActivityVideoWindowController(this)
        val dependencies = (application as ComposeScaffoldApplication).dependencies
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VideoDetailRoute(
                        videoId = arguments.videoId,
                        source = arguments.source,
                        videoRepository = dependencies.feedVideoRepository,
                        videoPlayerFactory = dependencies.videoPlayerFactory,
                        taskObserver = dependencies.viewModelTaskObserver,
                        onBack = onBackPressedDispatcher::onBackPressed,
                        onWindowModeChanged = videoWindowController::apply
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (::videoWindowController.isInitialized && !isChangingConfigurations) {
            videoWindowController.restore()
        }
        super.onDestroy()
    }

    companion object {
        /** 创建只携带稳定视频 ID 和来源的显式详情 Intent。 */
        fun createIntent(context: Context, arguments: VideoRouteArguments): Intent =
            Intent(context, VideoDetailActivity::class.java).putExtras(arguments.toBundle())
    }
}
