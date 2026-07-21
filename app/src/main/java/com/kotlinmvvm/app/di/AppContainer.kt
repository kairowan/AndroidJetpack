package com.kotlinmvvm.app.di

import android.content.Context
import android.util.Log
import com.kotlinmvvm.app.BuildConfig
import com.kotlinmvvm.app.config.AppNavigationMode
import com.kotlinmvvm.app.config.AppEnvironment
import com.kotlinmvvm.app.config.AppNetworkEndpoints
import com.kotlinmvvm.app.config.AppRemoteUrlPolicies
import com.kotlinmvvm.core.network.R as NetworkR
import com.kotlinmvvm.core.network.client.NetworkClientFactory
import com.kotlinmvvm.core.network.client.RemoteResourceHttpClientFactory
import com.kotlinmvvm.core.network.config.NetworkConfig
import com.kotlinmvvm.core.network.interceptor.NetworkLogFormatter
import com.kotlinmvvm.core.network.interceptor.NetworkLogLabels
import com.kotlinmvvm.core.network.interceptor.NetworkLogger
import com.kotlinmvvm.core.network.interceptor.NetworkLoggingInterceptor
import com.kotlinmvvm.core.network.interceptor.SegmentedNetworkLogger
import com.kotlinmvvm.data.feed.di.createFeedDataBindings
import com.kotlinmvvm.app.observability.AppDiagnosticObserver
import com.kotlinmvvm.core.ui.viewmodel.ViewModelTaskObserver
import com.kotlinmvvm.core.player.api.VideoPlayerFactory
import com.kotlinmvvm.core.player.factory.Media3VideoPlayerFactory
import coil.ImageLoader
import com.kotlinmvvm.domain.feed.repository.FeedPageRepository
import com.kotlinmvvm.domain.feed.repository.FeedVideoRepository
import java.io.File

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用进程依赖容器，只提供共享基础设施并通过各数据模块装配入口持有长生命周期依赖
 */
class AppContainer(
    context: Context
) : AppDependencies {
    private val applicationContext = context.applicationContext

    /** 当前构建选择的页面宿主模式。 */
    override val navigationMode: AppNavigationMode =
        AppNavigationMode.from(BuildConfig.APP_NAVIGATION_MODE)

    private val environment = AppEnvironment.from(BuildConfig.APP_ENVIRONMENT)

    private val networkConfig = NetworkConfig(
        userAgent = "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME}"
    )

    private val diagnosticObserver by lazy {
        AppDiagnosticObserver(
            tag = applicationContext.getString(com.kotlinmvvm.app.R.string.app_diagnostic_log_tag),
            taskMessageFormat = applicationContext.getString(
                com.kotlinmvvm.app.R.string.app_diagnostic_task_format
            ),
            networkMessageFormat = applicationContext.getString(
                com.kotlinmvvm.app.R.string.app_diagnostic_network_format
            ),
            cacheMessageFormat = applicationContext.getString(
                com.kotlinmvvm.app.R.string.app_diagnostic_cache_format
            ),
            logTaskLifecycle = BuildConfig.DEBUG,
            logFailureCauses = BuildConfig.DEBUG
        )
    }

    private val networkClientFactory by lazy {
        NetworkClientFactory(
            config = networkConfig,
            cacheDirectory = File(applicationContext.cacheDir, "network_http"),
            additionalInterceptors = listOfNotNull(
                if (BuildConfig.DEBUG) createNetworkLoggingInterceptor(applicationContext) else null
            )
        )
    }

    private val remoteResourceHttpClient by lazy {
        RemoteResourceHttpClientFactory(
            config = networkConfig,
            policy = AppRemoteUrlPolicies.feedMedia
        ).create()
    }

    override val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(applicationContext)
            .okHttpClient(remoteResourceHttpClient)
            .build()
    }

    override val videoPlayerFactory: VideoPlayerFactory by lazy {
        Media3VideoPlayerFactory(applicationContext, remoteResourceHttpClient)
    }

    private val feedBindings by lazy {
        createFeedDataBindings(
            networkClientFactory = networkClientFactory,
            endpoint = AppNetworkEndpoints.feed(environment),
            remoteResourceUrlPolicy = AppRemoteUrlPolicies.feedMedia,
            cacheDirectory = File(applicationContext.filesDir, "feed_cache"),
            networkFailureObserver = diagnosticObserver,
            dataObserver = diagnosticObserver
        )
    }

    /** 列表页面共享的 Feed 分页仓库角色。 */
    override val feedPageRepository: FeedPageRepository
        get() = feedBindings.pageRepository

    /** 详情页面共享的 Feed 视频仓库角色。 */
    override val feedVideoRepository: FeedVideoRepository
        get() = feedBindings.videoRepository

    /** 页面异步任务统一诊断入口。 */
    override val viewModelTaskObserver: ViewModelTaskObserver
        get() = diagnosticObserver

    private fun createNetworkLoggingInterceptor(context: Context): NetworkLoggingInterceptor {
        val logTag = context.getString(NetworkR.string.core_network_log_tag)
        return NetworkLoggingInterceptor(
            logger = SegmentedNetworkLogger(
                NetworkLogger { message -> Log.d(logTag, message) }
            ),
            formatter = NetworkLogFormatter(
                labels = NetworkLogLabels.from(context.resources)
            ),
            temporaryDirectory = File(context.cacheDir, "network_log")
        )
    }
}
