package com.example.flowdownload

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载库运行配置，负责声明存储、并发、重试、HTTP 栈和通知策略等基础行为。
 */
data class FlowDownloadConfig(
    /**
     * Room 数据库名称。
     */
    val databaseName: String = "flowdownload.db",
    /**
     * 进程内模式下允许同时执行的最大下载数。
     */
    val maxConcurrentDownloads: Int = 2,
    /**
     * HTTP 连接超时时间，单位毫秒。
     */
    val connectTimeoutMillis: Int = 15_000,
    /**
     * HTTP 读取超时时间，单位毫秒。
     */
    val readTimeoutMillis: Int = 15_000,
    /**
     * 进度写库与事件分发的节流时间，单位毫秒。
     */
    val progressThrottleMillis: Long = 200L,
    /**
     * 自动重试的基础退避时间，单位毫秒。
     */
    val retryBackoffMillis: Long = 30_000L,
    /**
     * 初始化后是否自动恢复排队中的任务。
     */
    val autoRecoverQueuedDownloads: Boolean = true,
    /**
     * 初始化后是否把被中断的运行中任务恢复为可继续的暂停状态。
     */
    val autoPauseInterruptedDownloads: Boolean = true,
    /**
     * 运行模式，支持 `IN_PROCESS` 和 `WORK_MANAGER`。
     */
    val runtimeMode: DownloadRuntimeMode = DownloadRuntimeMode.IN_PROCESS,
    /**
     * 下载 HTTP 实现，支持 OkHttp 和 Retrofit。
     */
    val httpStack: DownloadHttpStack = DownloadHttpStack.OKHTTP,
    /**
     * 自定义 HTTP 组件工厂类名，用于后台恢复场景重新实例化。
     */
    val httpComponentFactoryClassName: String? = null,
    /**
     * 通知相关配置。
     */
    val notification: FlowDownloadNotificationConfig = FlowDownloadNotificationConfig(),
)
