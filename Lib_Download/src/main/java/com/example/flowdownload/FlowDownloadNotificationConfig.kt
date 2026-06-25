package com.example.flowdownload

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载通知配置，负责声明通知开关、渠道信息、终态提示和动作按钮策略。
 */
data class FlowDownloadNotificationConfig(
    /**
     * 是否启用库内系统通知。
     */
    val enabled: Boolean = true,
    /**
     * 下载通知使用的通知渠道 ID。
     */
    val channelId: String = "flowdownload.active",
    /**
     * 下载通知使用的通知渠道名称。
     */
    val channelName: String = "Flow Download",
    /**
     * 是否在成功、失败、取消等终态后继续展示终态通知。
     */
    val showTerminalNotifications: Boolean = true,
    /**
     * 是否在通知中展示暂停、继续、取消、重试等控制动作。
     */
    val showControlActions: Boolean = true,
    /**
     * 是否在成功通知中展示打开或安装等文件动作。
     */
    val showTerminalActions: Boolean = true,
    /**
     * 是否在成功通知中额外展示分享动作。
     */
    val includeShareActionInTerminalActions: Boolean = false,
    /**
     * 自定义通知渲染器类名，用于后台恢复或跨进程重建时反射创建实例。
     */
    val rendererClassName: String? = null,
)
