package com.example.flowdownload

import android.content.Context
import com.example.flowdownload.download.data.DefaultDownloadRepository
import com.example.flowdownload.download.data.DownloadScheduler
import com.example.flowdownload.download.data.DownloadStore
import com.example.flowdownload.download.model.SystemClock
import com.example.flowdownload.download.worker.AndroidInProcessDownloadNotificationDispatcher
import com.example.flowdownload.download.worker.InProcessDownloadNotifier
import com.example.flowdownload.download.worker.WorkManagerDownloadScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载库统一入口，负责初始化单例客户端并向宿主暴露可复用的下载能力。
 */
object FlowDownload {
    @Volatile
    private var instance: FlowDownloadClient? = null

    @Volatile
    private var rememberedConfig: FlowDownloadConfig? = null

    private val lock = Any()

    /**
     * 记录最近一次下载配置，供后续惰性创建客户端时复用。
     *
     * 同时支持注入自定义 HTTP 组件工厂和通知渲染器。
     */
    fun configure(
        config: FlowDownloadConfig = FlowDownloadConfig(),
        httpComponentFactory: FlowDownloadHttpComponentFactory? = null,
        notificationRenderer: FlowDownloadNotificationRenderer? = null,
    ) {
        synchronized(lock) {
            rememberConfigurationLocked(config, httpComponentFactory, notificationRenderer)
        }
    }

    /**
     * 使用给定配置初始化或替换当前下载客户端。
     *
     * 同时支持注入自定义 HTTP 组件工厂和通知渲染器。
     */
    fun initialize(
        context: Context,
        config: FlowDownloadConfig = FlowDownloadConfig(),
        httpComponentFactory: FlowDownloadHttpComponentFactory? = null,
        notificationRenderer: FlowDownloadNotificationRenderer? = null,
    ): FlowDownloadClient = synchronized(lock) {
        rememberConfigurationLocked(config, httpComponentFactory, notificationRenderer)
        replaceClientLocked(context.applicationContext, config)
    }

    /**
     * 获取当前下载客户端；如果尚未初始化，会使用默认配置惰性创建。
     */
    fun get(context: Context): FlowDownloadClient {
        instance?.let { return it }
        return synchronized(lock) {
            instance ?: createClient(
                context = context.applicationContext,
                config = rememberedConfig ?: FlowDownloadConfig(),
            ).also { client ->
                instance = client
            }
        }
    }

    /**
     * 在通知动作或后台恢复场景下按指定配置确保客户端可用。
     */
    internal fun ensureClient(
        context: Context,
        config: FlowDownloadConfig,
    ): FlowDownloadClient = synchronized(lock) {
        rememberedConfig = config
        val current = instance
        if (current != null && current.config == config) {
            return current
        }
        replaceClientLocked(context.applicationContext, config)
    }

    /**
     * 记录最近一次有效配置，供动作桥接和后台恢复场景读取。
     */
    internal fun rememberConfig(config: FlowDownloadConfig) {
        rememberedConfig = config
    }

    /**
     * 获取当前或最近一次记录的配置。
     */
    internal fun peekConfig(): FlowDownloadConfig? = instance?.config ?: rememberedConfig

    /**
     * 关闭并清空当前客户端单例。
     */
    fun reset() {
        synchronized(lock) {
            instance?.shutdown()
            instance = null
            rememberedConfig = null
        }
    }

    /**
     * 安装一个运行时 HTTP 组件工厂。
     */
    fun installHttpComponentFactory(factory: FlowDownloadHttpComponentFactory) {
        FlowDownloadHttpComponentFactories.install(factory)
    }

    /**
     * 清除当前运行时 HTTP 组件工厂。
     */
    fun clearHttpComponentFactory() {
        FlowDownloadHttpComponentFactories.clear()
    }

    /**
     * 安装一个运行时通知渲染器。
     */
    fun installNotificationRenderer(renderer: FlowDownloadNotificationRenderer) {
        FlowDownloadNotificationRenderers.install(renderer)
    }

    /**
     * 清除当前运行时通知渲染器。
     */
    fun clearNotificationRenderer() {
        FlowDownloadNotificationRenderers.clear()
    }

    private fun rememberConfigurationLocked(
        config: FlowDownloadConfig,
        httpComponentFactory: FlowDownloadHttpComponentFactory?,
        notificationRenderer: FlowDownloadNotificationRenderer?,
    ) {
        httpComponentFactory?.let(FlowDownloadHttpComponentFactories::install)
        notificationRenderer?.let(FlowDownloadNotificationRenderers::install)
        rememberedConfig = config
    }

    private fun replaceClientLocked(
        context: Context,
        config: FlowDownloadConfig,
    ): FlowDownloadClient {
        rememberedConfig = config
        instance?.shutdown()
        return createClient(context, config).also { client ->
            instance = client
        }
    }

    private fun createClient(
        context: Context,
        config: FlowDownloadConfig,
    ): FlowDownloadClient {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val components = FlowDownloadComponents.create(
            context = context,
            config = config,
            clock = SystemClock,
        )
        val store: DownloadStore = components.store
        val scheduler = when (config.runtimeMode) {
            DownloadRuntimeMode.WORK_MANAGER -> WorkManagerDownloadScheduler(
                context = context,
                store = store,
                config = config,
            )

            DownloadRuntimeMode.IN_PROCESS -> DownloadScheduler(
                store = store,
                appScope = scope,
                engine = components.engine,
                clock = SystemClock,
                maxConcurrentDownloads = config.maxConcurrentDownloads,
                retryBackoffMillis = config.retryBackoffMillis,
            )
        }
        val inProcessNotifier = if (config.runtimeMode == DownloadRuntimeMode.IN_PROCESS) {
            InProcessDownloadNotifier(
                snapshots = store.observeAll(),
                dispatcher = AndroidInProcessDownloadNotificationDispatcher(
                    context = context,
                    config = config.notification,
                ),
                showTerminalNotifications = config.notification.showTerminalNotifications,
            )
        } else {
            null
        }

        return DefaultDownloadRepository(
            store = store,
            scheduler = scheduler,
            appScope = scope,
            clock = SystemClock,
            config = config,
            destinationAccess = components.destinationAccess,
            onStart = {
                inProcessNotifier?.start(scope)
            },
            onShutdown = {
                inProcessNotifier?.stop()
                scope.cancel()
                components.database.close()
            },
        )
    }
}
